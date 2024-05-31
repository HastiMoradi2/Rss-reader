import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.*;
import java.util.InputMismatchException;
import java.util.RandomAccess;
import java.util.Scanner;

public class Main {
    private static final int MAX_ITEMS = 5;
    static File file ;
    static Scanner s = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        int enterie = 0;
        while (enterie != 4) {
            System.out.println("Type a valid number for your desired action:");
            System.out.println("[1] Show updates\n[2] Add URL\n[3] Remove URL\n[4] Exit");
            try {
                enterie = s.nextInt();
                switch (enterie) {
                    case 1:
                        showUpdates();
                        break;
                    case 2:
                        addURL();
                        break;
                    case 3:
                        removeURL();
                        break;
                    case 4:
                        System.out.println("Exiting the program...");
                        break;
                    default:
                        System.out.println("Invalid input. Please enter a valid number.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                s.nextLine(); // Clear the input buffer
            }
        }
    }

    public static int lineCounter() throws IOException {
        int counter = 0;
        String line="";
        file = new File("data.txt");
        FileReader reader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(reader);
        while(bufferedReader.readLine()!=null)
            counter++;
        return counter;
    }

    public static void showUpdates() throws IOException {
        System.out.println("show updates for: ");
        String line = "";
        file = new File("data.txt");
        try {
            FileReader reader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String[] links;
            if (lineCounter() > 1) {
                System.out.println("[0] All websites");
                for (int i = 0; i < lineCounter(); i++) {
                    i++;
                    line = bufferedReader.readLine();
                    links = line.split(";");
                    System.out.println("[" + i + "]" + " " + links[0]);
                    i--;
                }
            } else if (lineCounter() == 0)
                System.out.println("there are no URLs in this file.");
            else if (lineCounter() == 1) {
                line = bufferedReader.readLine();
                links = line.split(";");
                System.out.println("[1] " + links[0]);

            }

            bufferedReader.close();
            FileReader reader1 = new FileReader(file);
            BufferedReader bufferedReader1 = new BufferedReader(reader1);
//            RandomAccessFile raf = new RandomAccessFile("data.txt","r");
//            raf.seek(0);
            int selected = s.nextInt();
            s.nextLine();
            if (selected == 0) {
                for (int i = 0; i < lineCounter(); i++) {
                    line = bufferedReader1.readLine();
                    links = line.split(";");
                    System.out.println(links[2]);
                    retrieveRssContent(links[2]);
                }
            } else if (selected == -1) {

            } else if(selected>lineCounter()||selected<=-2)
                System.out.println("Invalid number. Please enter a valid number.");
            else if (selected <= lineCounter() || selected > lineCounter()) {
                for (int i = 1; i <= lineCounter(); i++) {
                    line = bufferedReader1.readLine();
                    links = line.split(";");
                    if (i == selected) {
                        System.out.println(links[0]);
                        retrieveRssContent(links[2]);
                    }
                }
                bufferedReader1.close();
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid number.2");
            s.nextLine(); // Clear the input buffer
        }
    }

    //this method adds the url if it doesn't already exist in the file
    public static void addURL() throws Exception {
        System.out.println("Please enter the website's URL to add: ");
        String URL = s.next();
        s.nextLine();
        if(isValidURL(URL)) {
            if (checkIfURLExists(URL))
                System.out.println(URL + " already exists in the file.");
            else {
                try {
                    FileWriter writer = new FileWriter(new File("data.txt"), true);
                    BufferedWriter bw = new BufferedWriter(writer);
                    String HTMLSource = fetchPageSource(URL);
                    String htmlSourceLink = URL + "index.html";
                    bw.write(extractPageTitle(HTMLSource) + ";" + htmlSourceLink + ";" + extractRssUrl(URL) + "\n");
                    System.out.println("Added " + URL + " successfully.\n");
                    bw.close();
                } catch (IOException ex) {
                    System.out.println("the given URL isn't compatible");
                } catch (IllegalArgumentException ex) {
                    System.out.println("the given URL isn't compatible");
                }
            }
        }else if(!isValidURL(URL))
            System.out.println("the given URL isn't valid");
    }

    //this method removes the url if it exists in the file
    public static void removeURL() throws Exception {
        System.out.println("Please enter the website's URL to remove: ");
        String URL = s.next();
        if (isValidURL(URL)) {
            try {
                String stringToDelete = URL;
                File file = new File("data.txt");
                File tempFile = new File("tempFile.txt");

                try (FileWriter writer = new FileWriter(tempFile);
                     FileReader reader = new FileReader(file);
                     BufferedReader bufferedReader = new BufferedReader(reader);
                     BufferedWriter bufferedWriter = new BufferedWriter(writer)) {

                    String currentLine;
                    if (checkIfURLExists(URL)) {
                        while ((currentLine = bufferedReader.readLine()) != null) {
                            // Check if the line contains the string to delete
                            if (currentLine.contains(stringToDelete)) {
                                continue;
                            }
                            bufferedWriter.write(currentLine + "\n");
                        }
                        // Renaming the temporary file to the original file name
                        if (file.delete() && tempFile.renameTo(file)) {
                            System.out.println(URL + " removed successfully");
                        } else {
                            System.out.println("Could not rename the file");
                        }
                    } else {
                        System.out.println(URL + " doesn't exist in the file.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("The given URL isn't valid");
        }
    }

    //this method checks if the given url already exists in the file
    public static boolean checkIfURLExists(String URL) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(URL)) {
                    return true;
                }
            }
            br.close();
        }
        return false;
    }

    public static String extractPageTitle(String html)
{
        try
    {
    Document doc = Jsoup.parse(html);
    return doc.select("title").first().text();
    }
    catch (Exception e)
    {
        return "Error: no title tag found in page source!";
    }
}

    public static void retrieveRssContent(String rssUrl)
    {
        try {
            String rssXml = fetchPageSource(rssUrl);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            StringBuilder xmlStringBuilder = new StringBuilder();
            xmlStringBuilder.append(rssXml);
            ByteArrayInputStream input = new ByteArrayInputStream(
                    xmlStringBuilder.toString().getBytes("UTF-8"));
            org.w3c.dom.Document doc = documentBuilder.parse(input);
            NodeList itemNodes = doc.getElementsByTagName("item");

            for (int i = 0; i < MAX_ITEMS; ++i) {
                Node itemNode = itemNodes.item(i);
                if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) itemNode;
                    System.out.println("Title: " + element.getElementsByTagName("title").item(0).getTextContent());
                    System.out.println("Link: " + element.getElementsByTagName("link").item(0).getTextContent());
                    System.out.println("Description: " + element.getElementsByTagName("description").item(0).getTextContent());
                }
            }
        }
        //checked and found the exception in this website: https://stackoverflow.com/questions/8573176/internet-connection-error
        catch (UnknownHostException e){
            System.out.println("Check internet connection.");
        }
        catch (Exception e)
        {
            System.out.println("Error in retrieving RSS content for " + rssUrl + ": " + e.getMessage());
        }
    }


    public static String extractRssUrl(String url) throws IOException{
        Document doc = Jsoup.connect(url).get();
        return doc.select("[type='application/rss+xml']").attr("abs:href");
    }


    public static String fetchPageSource(String urlString) throws Exception {
        URI uri = new URI(urlString);
        URL url = uri.toURL();
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36");
        return toString(urlConnection.getInputStream());
    }

    //this method checks if a Given string is a valid URL
    //used this website to understand the method: https://stackoverflow.com/questions/3931696/how-to-verify-if-a-string-in-java-is-a-valid-url
    public static boolean isValidURL(String urlString) {
        try {
            URL url = new URL(urlString);
            url.toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String toString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String inputLine;
        StringBuilder stringBuilder = new StringBuilder();
        while ((inputLine = bufferedReader.readLine()) != null) {
            stringBuilder.append(inputLine);
        }

        return stringBuilder.toString();
    }

}