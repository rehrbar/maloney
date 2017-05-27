package ch.hsr.maloney.util;

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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO probably replace through reporting feature
public class SimpleQuery {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    protected MetadataStore metadataStore;
    protected DataSource dataSource;

    public void setContext(MetadataStore metadataStore, DataSource dataSource){
        this.metadataStore = metadataStore;
        this.dataSource = dataSource;
    }

    public void setFilter(String filter){
        // TODO do something with this filter
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
        // TODO format output
        // TODO filter fields
        sb.append(fileAttributes.getFileId());
        sb.append(" ");
        sb.append(fileAttributes.getFileName());
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
            switch (matcher.group(propertyGroupName)){
                case "fileId":
                    ruleComposite.addRule(fileAttributes -> fileAttributes.getFileId().toString().matches(value));
                    break;
                case "fileName":
                    ruleComposite.addRule(fileAttributes -> fileAttributes.getFileName().matches(value));
                    break;
                case "filePath":
                    ruleComposite.addRule(fileAttributes -> fileAttributes.getFilePath().matches(value));
                    break;
                case "dateAccessed":
                    ruleComposite.addRule(fileAttributes -> formatDate(fileAttributes.getDateAccessed()).matches(value));
                    break;
                case "dateChanged":
                    ruleComposite.addRule(fileAttributes -> formatDate(fileAttributes.getDateChanged()).matches(value));
                    break;
                case "dateCreated":
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
