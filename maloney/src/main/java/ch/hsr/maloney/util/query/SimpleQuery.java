package ch.hsr.maloney.util.query;

import ch.hsr.maloney.storage.Artifact;
import ch.hsr.maloney.storage.DataSource;
import ch.hsr.maloney.storage.FileAttributes;
import ch.hsr.maloney.storage.MetadataStore;
import ch.hsr.maloney.util.categorization.AndRuleComposite;
import ch.hsr.maloney.util.categorization.Category;
import ch.hsr.maloney.util.categorization.OrRuleComposite;
import ch.hsr.maloney.util.categorization.RuleComposite;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO Extract common functionality shared with reporting feature.
public class SimpleQuery {
    private static final String FIELD_DELIMITER = "\t";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    private List<PropertyName> propertiesToDisplay;
    protected MetadataStore metadataStore;
    protected DataSource dataSource;

    public SimpleQuery(){
        setFilter(null);
    }

    public void setContext(MetadataStore metadataStore, DataSource dataSource){
        this.metadataStore = metadataStore;
        this.dataSource = dataSource;
    }

    public void setFilter(String filter){
        propertiesToDisplay = new LinkedList<>();
        if(filter != null) {
            // Pattern matching all names separated by any non alphabetic character.
            final String propertyGroupName = "property";
            Pattern pattern = Pattern.compile("(?<" + propertyGroupName + ">[a-zA-Z]+)");
            Matcher matcher = pattern.matcher(filter);

            while (matcher.find()) {
                PropertyName p = PropertyName.getByFieldName(matcher.group(propertyGroupName));
                if (p != null) {
                    propertiesToDisplay.add(p);
                }
            }
        }

        // Fallback: Adding all properties
        if(propertiesToDisplay.isEmpty()) {
            propertiesToDisplay.addAll(Arrays.asList(PropertyName.values()));
        }
    }

    public void performQuery(OutputStream os, String query){
        final PrintStream printStream = new PrintStream(os);
        Category queryCategory = createQueryCategory(query);
        int counter = 0;
        Iterator<FileAttributes> iterator = metadataStore.iterator();
        while (iterator.hasNext()){
            FileAttributes fileAttributes = iterator.next();
            List<Artifact> artifacts = metadataStore.getArtifacts(fileAttributes.getFileId());

            if(isMatch(queryCategory, fileAttributes, artifacts)){
                writeToOutput(printStream, fileAttributes, artifacts);
                counter++;
            }
        }
        printStream.println("Results: "+ counter);
        printStream.close();
    }

    private void writeToOutput(PrintStream printStream, FileAttributes fileAttributes, List<Artifact> artifacts){
        StringBuilder sb = new StringBuilder();
        for(PropertyName prop : propertiesToDisplay){
            switch (prop){
                case FileId:
                    sb.append(fileAttributes.getFileId());
                    break;
                case FileName:
                    sb.append(fileAttributes.getFileName());
                    break;
                case FilePath:
                    sb.append(fileAttributes.getFilePath());
                    break;
                case DateAccessed:
                    sb.append(fileAttributes.getDateAccessed());
                    break;
                case DateChanged:
                    sb.append(fileAttributes.getDateChanged());
                    break;
                case DateCreated:
                    sb.append(fileAttributes.getDateCreated());
                    break;
                case Artifacts:
                    for(Artifact artifact : artifacts){
                        sb.append(artifact.getOriginator()).append(FIELD_DELIMITER);
                        sb.append(artifact.getType()).append(FIELD_DELIMITER);
                        sb.append(artifact.getValue());
                    }
                    break;
            }
            sb.append(FIELD_DELIMITER);
        }

        printStream.println(sb);
    }

    private boolean isMatch(Category query, FileAttributes fileAttributes, List<Artifact> artifacts) {
        return query.getRuleSet().match(fileAttributes);
    }

    protected static Category createQueryCategory(String query){
        // TODO prepare rule set for artifacts
        final String valueGroupName = "value";
        final String propertyGroupName = "property";
        final Pattern pattern = Pattern.compile("((?<" + propertyGroupName + ">[a-zA-Z]+)=\"(?<" + valueGroupName + ">[^\"]+))+\"");
        Matcher matcher = pattern.matcher(query);
        RuleComposite ruleComposite = new AndRuleComposite();
        while(matcher.find()){
            final String value = matcher.group(valueGroupName);
            switch (PropertyName.getByFieldName(matcher.group(propertyGroupName))){
                case FileId:
                    ruleComposite.addRule(fileAttributes -> fileAttributes.getFileId().toString().matches(value));
                    break;
                case FileName:
                    ruleComposite.addRule(fileAttributes -> fileAttributes.getFileName().matches(value));
                    break;
                case FilePath:
                    ruleComposite.addRule(fileAttributes -> fileAttributes.getFilePath().matches(value));
                    break;
                case DateAccessed:
                    ruleComposite.addRule(fileAttributes -> formatDate(fileAttributes.getDateAccessed()).matches(value));
                    break;
                case DateChanged:
                    ruleComposite.addRule(fileAttributes -> formatDate(fileAttributes.getDateChanged()).matches(value));
                    break;
                case DateCreated:
                    ruleComposite.addRule(fileAttributes -> formatDate(fileAttributes.getDateCreated()).matches(value));
                    break;
            }
        }
        if(!matcher.matches()) {
            // Fallback rule
            ruleComposite = new OrRuleComposite();
            ruleComposite.addRule(fileAttributes -> fileAttributes.getFileId().toString().contains(query));
            ruleComposite.addRule(fileAttributes -> fileAttributes.getFileName().contains(query));
        }

        // Required final for inner class.
        final RuleComposite finalComposite = ruleComposite;
        return new Category() {
            @Override
            public String getName() {
                return "Query";
            }

            @Override
            public RuleComposite getRuleSet() {
                return finalComposite;
            }
        };
    }

    @NotNull
    private static String formatDate(Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return localDateTime.format(DATE_TIME_FORMATTER);
    }
}
