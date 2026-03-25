
///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS info.picocli:picocli:4.6.3
//DEPS commons-io:commons-io:2.18.0

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.time.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * La qualité logicielle, ca dépend du contexte, et là il y en a pas trop
 *
 * Install jbang : ```curl -Ls https://sh.jbang.dev | bash -s - app setup`````
 *
 * Add this to bashrc :
 */
//`echo "# memo alias" >> ~/.bashrc ; echo "alias memo='cd /c/Users/wherever/you/put/it ; jbang ActivityTracker.java' >> ~/.bashrc ; source ~/.bashrc ; memo
/* 
 * And then you can open git bash anywhere, type "memo"
 */
@Command(name = "ActivityTracker", mixinStandardHelpOptions = true, version = "activity-tracker 0.1",
        description = "Activity tracking application made with jbang")
class ActivityTracker implements Callable<Integer> {

    private static final String DATE_PATTERN = "dd/MM/yyyy";
    private static final String TIME_PATTERN = "HH:mm";
    private static final Scanner scanner = new Scanner(System.in);

    @Parameters(index = "0", description = "Project name", defaultValue = "CAPGEMINI")
    private String project;

    public static void main(String... args) {
        int exitCode = new CommandLine(new ActivityTracker()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws IOException {
        printTitle();
        handleActivityTracking();
        return 0;
    }

    private void printTitle() {
        String title = """
            ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░    ░░░
            ░  ░░░░  ░        ░  ░░░░  ░░      ░░░░░░░░░░░░    ░░░░░
            ▒   ▒▒   ▒  ▒▒▒▒▒▒▒   ▒▒   ▒  ▒▒▒▒  ▒▒▒▒▒▒▒▒▒    ▒▒▒▒▒▒▒
            ▓        ▓      ▓▓▓        ▓  ▓▓▓▓  ▓▓▓▓▓▓▓    ▓▓▓▓▓▓▓▓▓
            █  █  █  █  ███████  █  █  █  ████  █████    ███████████
            █  ████  █        █  ████  ██      ████    █████████████
            █████████████████████████████████████    ███████████████
        """;

        System.out.println(
                title
        );

    }
    // printTitle()

    private void handleActivityTracking() {
        Config config = new Config(project);
        String fileName = config.getFileName();

        // Display existing files
        List<String> existingFiles = FileOperations.listFilesWithPattern(config.getNaming());
        existingFiles = existingFiles.stream().sorted().collect(Collectors.toList());
        existingFiles.forEach(this::displayPreviousEntries);
        printTitle();
        while (true) {
            addNewEntry(fileName);
            displayPreviousEntries(fileName);
        }

    }

    private void displayPreviousEntries(String filePath) {
        List<String> lines = FileOperations.readFile(filePath);
        lines.forEach(line -> {
            if (line.isEmpty() || line.trim().isBlank()) {
                return;
            }
            ActivityEntry entry = ActivityEntry.createActivityEntry(line, Config.SEPARATOR);

            String activityType = ColorConstants.GREEN + entry.activityType() + ColorConstants.RESET;
            String activityDescription = ColorConstants.LIGHT_BLUE + entry.activityDescription() + ColorConstants.RESET;
            String status = ColorConstants.RED + entry.status() + ColorConstants.RESET;
            String comment = ColorConstants.LIGHT_BLUE + entry.comment() + ColorConstants.RESET;
            String timestamp = ColorConstants.GREEN + entry.timestamp() + ColorConstants.RESET;
            Double minutes = null;
            try {
                if (!entry.timeSpent().trim().isBlank()) {
                    minutes = Double.parseDouble(entry.timeSpent());
                    minutes = minutes * 7.75 * 60;
                }
            } catch (NumberFormatException e) {
                //skip
            }
            String minutesStringDisplay = (minutes == null || minutes.equals(0.0)) ? "" : " ~ " + minutes + " mins";
            String time = ColorConstants.GREEN + (entry.timeSpent().isBlank() || entry.timeSpent().equals("0.0") ? "" : entry.timeSpent() + "j") + minutesStringDisplay + ColorConstants.RESET;

            String display = " [" + timestamp + (time.isBlank() ? "" : " " + time) + "] [ " + status + " ] " + activityType + " : " + activityDescription + " \n * [ " + comment + " ] \n";
            display=display.replaceAll("\\|", "\\\n\\\t");
            System.out.println(display);
        });

        // Calculate and display the daily total time spent
        double dailyTotalTime = lines.stream()
                .map(line -> ActivityEntry.createActivityEntry(line, Config.SEPARATOR))
                .filter(entry -> entry != null && !entry.timeSpent().isBlank())
                .mapToDouble(entry -> {
                    try {
                        return Double.parseDouble(entry.timeSpent());
                    } catch (NumberFormatException e) {
                        return 0.0;
                    }
                })
                .sum();

        // Call the new method to display the time summary
        displayTimeSummary(lines);

        System.out.println(ColorConstants.RED + "--- Daily Total Time Spent ---" + ColorConstants.RESET );
        System.out.printf("%s%.2f days %s ~ %.2f h %s\n", ColorConstants.GREEN, dailyTotalTime, ColorConstants.LIGHT_BLUE, dailyTotalTime * 7.75, ColorConstants.RESET);
        System.out.println(ColorConstants.RED + "_________________________________________\n" + ColorConstants.RESET);
    }

    private void displayTimeSummary(List<String> lines) {
        // Map to store total time spent per activity type
        Map<String, Double> timeSummary = new HashMap<>();

        for (String line : lines) {
            if (line.isEmpty() || line.trim().isBlank()) {
                continue;
            }
            ActivityEntry entry = ActivityEntry.createActivityEntry(line, Config.SEPARATOR);

            if (entry == null || entry.timeSpent().isBlank()) {
                continue;
            }

            try {
                double timeSpent = Double.parseDouble(entry.timeSpent());
                timeSummary.merge(entry.activityType().trim().toUpperCase(), timeSpent, Double::sum);
            } catch (NumberFormatException e) {
                System.out.println("Failed to parse time spent for entry: " + line + "\n" + e.getMessage());
            }
        }

        // Display the summary
        System.out.println(ColorConstants.LIGHT_BLUE + "\n--- Daily Time Summary ---" + ColorConstants.RESET);
        timeSummary.forEach((activityType, totalTime) -> {
            Double estimatedHoursSpent = totalTime * 7.75; // 7h45 
            Double estimatedMinutes = estimatedHoursSpent * 60;
            System.out.printf("%s: %s%.2f days %s ~ %.2f h %s %s\n", ColorConstants.GREEN + activityType, ColorConstants.LIGHT_BLUE, totalTime, ColorConstants.RED, estimatedHoursSpent, (estimatedMinutes < 60) ? estimatedMinutes + " minutes" : "", ColorConstants.RESET);
        });
        System.out.println(ColorConstants.LIGHT_BLUE + "--------------------------\n" + ColorConstants.RESET);
    }

    private void displayCurrentEntries(String filePath) {
        File currentFile = new File(filePath);
        if (!currentFile.exists()) {
            try {
                currentFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Failed to create the file " + filePath);
            }
        }
        displayPreviousEntries(filePath);
    }

    private void addNewEntry(String filePath) {
        System.out.println(ColorConstants.RED + """
                PCG2-2248 pour logger votre temps de cérémonies: 
                donc vous loggez uniquement du temps de "cérémonies" quand celles-ci vous prennent + de 2h dans une journée. 
                
                On ne log pas du temps de support, 
                il y a celle-ci pour ça: PCG2-2148, quand vous aidez qqun sur du debug ou vous avez des metings en lien avec tel ou tel topic tehcnique/onfcitonnl, ça rentre là-dedans, ce n'est pas du temps de cérémonies!
                """ + ColorConstants.RESET);
        String activity = read("Entrez une activité", "DEV TEST CEREMONY LEARNING CONTINUOUS_IMPROVEMENT SUPPORT CAPGEMINI DOCUMENTATION", false);
        String description = read("Description", null, false);
        String status = read("Statut", "TODO, DOING, DONE, NOTE", true);
        String comment = read("Commentaire", null, true);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_PATTERN + " " + TIME_PATTERN));
        String time = read("Saisissez un temps en proportion de jour", "1h= 0.125\n 1h20=0.2\n 1h45= 0.25\n 2h10= 0.3\n 3h3= 0.5\n 5h20= 0.75\n 7h= 1", true);
        double timeVerified = 0;
        try {
            if (!time.trim().isBlank()) {
                timeVerified = Double.parseDouble(time);
            }
        } catch (NumberFormatException e) {
            System.out.println(time + " n'est pas une saisie de temps en proportion de jour, ne sera pas sauvegardé.");
        }

        String entry = String.join(Config.SEPARATOR, project, activity, description, status, comment, timestamp, String.valueOf(timeVerified));
        FileOperations.appendToFile(filePath, entry);
    }

    private static String read(String question, String example, boolean canBeEmpty) {
        System.out.println(ColorConstants.LIGHT_BLUE + "--> " + ColorConstants.GREEN + question + ColorConstants.RESET);
        if (example != null && !example.isBlank()) {
            System.out.println(ColorConstants.LIGHT_BLUE + example + ColorConstants.RESET);
        }
        while (!scanner.hasNextLine()) {
        }
        String response = scanner.nextLine();
        return (response.trim().isBlank())
                ? (canBeEmpty ? "" : read(question, example, canBeEmpty))
                : response;
    }

    class Config {

        static final String SEPARATOR = ";";
        private String naming;

        public Config(String project) {
            this.naming = project + "_suivi_activite";
        }

        public String getFileName() {
            return naming + "_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";
        }

        public String getNaming() {
            return naming;
        }
    } // Config class

    class FileOperations {

        public static List<String> listFilesWithPattern(String pattern) {
            ArrayList<String> list = new ArrayList<>();
            try {
                list = (ArrayList<String>) Files.list(Path.of("."))
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .filter(fileName -> fileName.startsWith(pattern))
                        .collect(Collectors.toList());
            } catch (IOException e) {
                System.out.println("An error occured while fetching the file list. " + e.getMessage());
            }
            return list;
        }

        public static List<String> readFile(String filePath) {
            try {
                return Files.readAllLines(Path.of(filePath));
            } catch (IOException e) {
                throw new RuntimeException("Failed to read file: " + filePath, e);
            }
        }

        public static void appendToFile(String filePath, String content) {
            try {
                System.out.println("Enregister ? (o/n) : ");
                if (scanner.hasNext() && scanner.nextLine().toLowerCase().startsWith("o")) {
                    Path path = Path.of(filePath);
                    // Normalize line endings to LF and write with UTF-8 encoding
                    String normalizedContent = content.replace("\r\n", "\n").replace("\r", "\n") + "\n";
                    Files.writeString(path, normalizedContent, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
                    System.out.println("Sauvegardé avec succès!");
                } else if (scanner.nextLine().toLowerCase().startsWith("n")) {
                    return;
                } else {
                    appendToFile(filePath, content);
                }
            } catch (IOException e) {
                System.err.println("Erreur lors de la sauvegarde: " + e.getMessage());
            }
        }
    } // FileOperations class

    record ActivityEntry(String activityType, String activityDescription, String status, String comment, String timestamp, String timeSpent) {

        public static ActivityEntry createActivityEntry(String line, String separator) {
            if (line == null || line.trim().isBlank()) {
                return null;
            }
            String[] parts = line.split(separator);
            //String company= parts[0];

            String activityType = (parts.length > 1) ? parts[1] : "";
            String activityDescription = (parts.length > 2) ? parts[2] : "";
            String status = (parts.length > 3) ? parts[3] : "";
            String comment = (parts.length > 4) ? parts[4] : "";
            String timestamp = (parts.length > 5) ? parts[5] : "";
            String timeSpent = (parts.length > 6) ? parts[6] : "";

            return new ActivityEntry(activityType, activityDescription, status, comment, timestamp, timeSpent);
        }

    } // ActivityEntry class

    abstract class ColorConstants {

        public static final String GREEN = "\u001B[32m";
        public static final String BLUE = "\u001B[0;40;34m";
        public static final String RED = "\u001B[0;40;91m";
        public static final String LIGHT_BLUE = "\u001B[0;40;96m";
        public static final String RESET = "\u001B[0m";

    } // ColorConstants class

} // ActivityTracker class
