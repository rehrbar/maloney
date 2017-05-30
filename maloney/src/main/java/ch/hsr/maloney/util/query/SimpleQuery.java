package ch.hsr.maloney.util.query;

import ch.hsr.maloney.storage.Artifact;
import ch.hsr.maloney.storage.FileAttributes;
import ch.hsr.maloney.storage.MetadataStore;
import ch.hsr.maloney.util.categorization.*;
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

/**
 * This class provides query capabilities
 */
public class SimpleQuery {
    private static final String FIELD_DELIMITER = "\t";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    protected MetadataStore metadataStore;
    private List<PropertyName> propertiesToDisplay;

    /**
     * Creates an instance of {@link SimpleQuery} with the default filter.
     * @param metadataStore Source used for this query.
     */
    public SimpleQuery(MetadataStore metadataStore) {
        this.metadataStore = metadataStore;
        setFilter(null);
    }

    /**
     * Creates an instance of {@link Category} based on the provided query.
     *
     * @param query Combination of property names and corresponding regular expressions, which are used to create the
     *              rule set. If the query does not match the pattern, the default query on fileId and fileName is used.
     *              For example: fileName="reg.?"
     * @return A new instance matching the query.
     */
    static Category createQueryCategory(String query) {
        final String valueGroupName = "value";
        final String propertyGroupName = "property";
        final Pattern pattern = Pattern.compile("((?<" + propertyGroupName + ">[a-zA-Z]+)=\"(?<" + valueGroupName + ">[^\"]+))+\"");
        Matcher matcher = pattern.matcher(query);
        List<RuleComponent> components = new LinkedList<>();
        while (matcher.find()) {
            final String value = matcher.group(valueGroupName);
            switch (PropertyName.getByFieldName(matcher.group(propertyGroupName))) {
                case FileId:
                    components.add(fileAttributes -> fileAttributes.getFileId().toString().matches(value));
                    break;
                case FileName:
                    components.add(fileAttributes -> fileAttributes.getFileName().matches(value));
                    break;
                case FilePath:
                    components.add(fileAttributes -> fileAttributes.getFilePath().matches(value));
                    break;
                case DateAccessed:
                    components.add(fileAttributes -> formatDate(fileAttributes.getDateAccessed()).matches(value));
                    break;
                case DateChanged:
                    components.add(fileAttributes -> formatDate(fileAttributes.getDateChanged()).matches(value));
                    break;
                case DateCreated:
                    components.add(fileAttributes -> formatDate(fileAttributes.getDateCreated()).matches(value));
                    break;
                case ArtifactOriginator: // Matches in artifacts does not have to be in the same artifact.
                    components.add(fileAttributes -> fileAttributes.getArtifacts().stream().anyMatch(artifact -> artifact.getOriginator().matches(value)));
                    break;
                case ArtifactType:
                    components.add(fileAttributes -> fileAttributes.getArtifacts().stream().anyMatch(artifact -> artifact.getType().matches(value)));
                    break;
                case ArtifactValue:
                    components.add(fileAttributes -> fileAttributes.getArtifacts().stream().anyMatch(artifact -> artifact.getValue().toString().matches(value)));
                    break;
            }
        }

        final RuleComposite ruleComposite;
        if (!components.isEmpty()) {
            ruleComposite = new AndRuleComposite();
            components.forEach(ruleComposite::addRule);
        } else {
            // Fallback rule
            ruleComposite = new OrRuleComposite();
            ruleComposite.addRule(fileAttributes -> fileAttributes.getFileId().toString().contains(query));
            ruleComposite.addRule(fileAttributes -> fileAttributes.getFileName().contains(query));
        }

        return new Category() {
            @Override
            public String getName() {
                return "Query";
            }

            @Override
            public RuleComposite getRules() {
                return ruleComposite;
            }
        };
    }

    @NotNull
    private static String formatDate(Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return localDateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * Sets the filter for the output. If not other specified, the default filter is used and all properties are printed.
     *
     * @param filter List of properties matching {@link PropertyName}. If there are no matches or null, all properties
     *               are selected known to {@link PropertyName}. For example, use "fileName fileId".
     */
    public void setFilter(String filter) {
        propertiesToDisplay = new LinkedList<>();
        if (filter != null) {
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
        if (propertiesToDisplay.isEmpty()) {
            propertiesToDisplay.addAll(Arrays.asList(PropertyName.values()));
        }
    }

    /**
     * Performs the query and prints everything to the output stream.
     *
     * @param os    Stream where the output is redirected.
     * @param query Query to select which files should be printed to the stream.
     */
    public void performQuery(OutputStream os, String query) {
        final PrintStream printStream = new PrintStream(os);
        Category queryCategory = createQueryCategory(query);
        int counter = 0;
        Iterator<FileAttributes> iterator = metadataStore.iterator();
        writeHeader(printStream);
        while (iterator.hasNext()) {
            FileAttributes fileAttributes = iterator.next();

            if (isMatch(queryCategory, fileAttributes)) {
                writeToOutput(printStream, fileAttributes);
                counter++;
            }
        }
        printStream.println("Results: " + counter);
        printStream.close();
    }

    /**
     * Writes a header for the given filter to the output stream.
     *
     * @param printStream The output stream.
     */
    private void writeHeader(PrintStream printStream) {
        StringBuilder sb = new StringBuilder();
        for (PropertyName prop : propertiesToDisplay) {
            sb.append(prop.getFieldName()).append(FIELD_DELIMITER);
        }
        printStream.println(sb);
    }

    /**
     * Writes {@link FileAttributes} to the output stream. Order and which attributes are printed is dependent on the filter.
     *
     * @param printStream    The output stream.
     * @param fileAttributes Attributes which should be written.
     */
    private void writeToOutput(PrintStream printStream, FileAttributes fileAttributes) {
        StringBuilder sb = new StringBuilder();
        for (PropertyName prop : propertiesToDisplay) {
            switch (prop) {
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
                    for (Artifact artifact : fileAttributes.getArtifacts()) {
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

    private boolean isMatch(Category query, FileAttributes fileAttributes) {
        return query.getRules().match(fileAttributes);
    }
}
