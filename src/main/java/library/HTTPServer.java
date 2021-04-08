package library;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HTTPServer {

    static final File WEB_ROOT = new File("lib/");
    static final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "404.html";
    static final String METHOD_NOT_SUPPORTED = "not_supported.html";
    static final int PORT = 8080;

    public static void main(String[] args) {
        ServerSocket server = null;
        try {
            server = new ServerSocket(PORT);
            System.out.println("Server started.\n Listening for connections on port: " + PORT + "...\n");

            while (true) {
                Socket connection = server.accept();
                System.out.println("Connection opened. (" + new Date() + ")");
                runServer(connection);
            }

        } catch (IOException e) {
            System.out.println("Error while accepting request!");
        } finally {
            try {
                server.close();

            } catch (IOException e) {
                System.out.println("Unable to close the server socket!");

            }
        }

    }

    private static void runServer(Socket connection) {
        BufferedReader request = null;
        BufferedOutputStream response = null;
        try {
            request = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            response = new BufferedOutputStream(connection.getOutputStream());

            String[] fragments = request.readLine().split(" ");
            String method = fragments[0].toUpperCase();
            String fileRequested = fragments[1].toLowerCase();

            StringBuilder requestBody = new StringBuilder();
            String line;
            Map<String, String> headers = new HashMap<>();
            while (request.ready()) {
                line = request.readLine();
                if (line.trim().isEmpty()) {
                    break;
                } else {
                    String[] parts = line.split(":");
                    headers.put(parts[0].trim(), parts[1].trim());
                }
            }
            try {
                while (request.ready()) {
                    line = request.readLine();
                    requestBody.append(line);
                    requestBody.append(System.getProperty("line.separator"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
                replyMethodNotSupported(response, method);

            } else {
                try {
                    fileRequested = replyWithRequestedFile(response, fileRequested, requestBody.toString(), method);

                } catch (FileNotFoundException fnfe) {
                    try {
                        respondFileNotFound(response, fileRequested, method);

                    } catch (IOException ioe) {
                        System.out.println("Error with file not found exception: " + ioe.getMessage());
                    }
                }
            }

        } catch (IOException ioe) {
            replyServerError(ioe);

        } finally {
            closeConnection(connection, request, response);
        }
    }

    private static void closeConnection(Socket connect, BufferedReader in, BufferedOutputStream dataOut) {
        try {
            in.close();
            dataOut.close();
            connect.close();

        } catch (Exception e) {
            System.err.println("Error closing stream: " + e.getLocalizedMessage());

        }

        System.out.println("Connection closed.\n");
    }

    private static void replyServerError(IOException ioe) {
        System.out.println("Server error: " + ioe);
    }

    private static String replyWithRequestedFile(BufferedOutputStream response, String fileRequested,
            String requestBody, String method) throws IOException {

        String fileRe = fileRequested;

        if (fileRequested.endsWith("/") || fileRequested.equals("/uservalidation/")) {
            fileRequested += DEFAULT_FILE;
        }

        File file = new File(WEB_ROOT, DEFAULT_FILE);
        int fileLength = (int) file.length();
        String content = getContentType(fileRequested);

        byte[] fileData = readFileData(file, fileLength);

        formatHttpResponseHeader(response, "200 OK", fileLength, content);

        if (method.equals("GET")) {
            response.write(fileData, 0, fileLength);
        }

        if (method.equals("POST")) {
            response.write(fileData, 0, fileLength);
            StringBuilder bodyFormat = new StringBuilder();
            bodyFormat.append(System.getProperty("line.separator"));
            bodyFormat.append("Requested body is: \n");
            bodyFormat.append("-------------------\n");

            if (fileRe.equals("/usertextupload/")) {
                bodyFormat.append(System.getProperty("line.separator"));
            }

            if (fileRe.equals("/orm-lib")) {
                String[] questionStrings = requestBody.split(" ");
                String questionNumber = questionStrings[5];
                bodyFormat.append(requestBody);

                LibraryDatabase database = LibraryDatabase.getInstance();

                try {
                    switch (questionNumber) {
                    case "QUESTION1":
                        String bookInformationString = requestBody.substring(requestBody.lastIndexOf(":") + 1)
                                .replace("\r\n", "");
                        String[] bookInformation = bookInformationString.split(",");
                        database.insertBookInformation(bookInformation[0], bookInformation[1], bookInformation[2]);
                        bodyFormat.append("Insert completed!");
                        break;

                    case "QUESTION2":
                        String updateUserString = requestBody.substring(requestBody.lastIndexOf(":") + 1)
                                .replace("\r\n", "");
                        String[] updateUser = updateUserString.split(",");
                        database.question2(updateUser[0], updateUser[1], updateUser[2]);
                        bodyFormat.append("Update completed!");
                        break;

                    case "QUESTION3":
                        String bookStockId = requestBody.substring(requestBody.lastIndexOf(":") + 1).replace("\r\n",
                                "");
                        database.deleteLoanRecord(bookStockId);
                        bodyFormat.append("Delete completed!");
                        break;

                    case "QUESTION4":
                        String titleString = requestBody.substring(requestBody.lastIndexOf(":") + 1).replace("\r\n",
                                "");
                        bodyFormat.append(database.question4(titleString));
                        break;

                    case "QUESTION5":
                        String branchName = requestBody.substring(requestBody.lastIndexOf(":") + 1).replace("\r\n", "");
                        bodyFormat.append(database.question5(branchName));
                        break;

                    case "QUESTION6":
                        bodyFormat.append(database.question6());
                        break;

                    default:
                        bodyFormat.append("Method not implemented");
                        break;
                    }
                } catch (SQLException e) {
                    bodyFormat.append("Something went wrong: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            response.write(bodyFormat.toString().getBytes());
            response.flush();
        }
        response.flush();
        System.out.println("File " + fileRequested + " of type " + content + " returned!");

        return fileRequested;

    }

    private static void replyMethodNotSupported(BufferedOutputStream response, String method) throws IOException {
        System.out.println("501 NOT IMPLEMENTED: " + method + " method.");

        File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
        int fileLength = (int) file.length();
        String contentMimeType = "text/html";

        formatHttpResponseHeader(response, "501 not implemented", fileLength, contentMimeType);

        byte[] fileData = readFileData(file, fileLength);
        response.write(fileData, 0, fileLength);
        response.flush();
    }

    private static void formatHttpResponseHeader(BufferedOutputStream dataOut, String responseStatus, int fileLength,
            String contentMimeType) {
        final PrintWriter out = new PrintWriter(dataOut);
        out.println("HTTP/1.1 " + responseStatus);
        out.println("Server: Java HTTP Server from Di: 1.0");
        out.println("Date: " + new Date());
        out.println("Content-type: " + contentMimeType);
        out.println("Content-length: " + fileLength);
        out.println();
        out.flush();
    }

    private static byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];
        fileIn = new FileInputStream(file);
        fileIn.read(fileData);

        fileIn.close();
        return fileData;
    }

    private static String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".htm") || fileRequested.endsWith(".html"))
            return "text/html";
        else
            return "text/plain";
    }

    private static void respondFileNotFound(BufferedOutputStream response, String fileRequested, String method)
            throws IOException {
        File file = new File(WEB_ROOT, FILE_NOT_FOUND);
        int fileLength = (int) file.length();

        formatHttpResponseHeader(response, "404 file not found", fileLength, "text/html");

        byte[] fileData = readFileData(file, fileLength);
        if (method.equals("GET")) {
            response.write(fileData, 0, fileLength);
        }
        response.flush();
        System.out.println("File " + fileRequested + " not found!");
    }
}
