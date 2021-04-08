package library;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HTTPServer {
    // file definition, change the web_root to your absolute path if needed.
    // All the files you can define in your own folder and style.
    static final File WEB_ROOT = new File("lib/");
    // Default file is for successful request.
    static final String DEFAULT_FILE = "index.html";
    // 404 file is for not found request.
    static final String FILE_NOT_FOUND = "404.html";
    // not_supported file is for not supported methods.
    // Currently this server can accept GET, HEAD and POST methods.
    static final String METHOD_NOT_SUPPORTED = "not_supported.html";
    // server port definition.
    static final int PORT = 8080;

    public static void main(String[] args) {
        // serversocket definition.
        ServerSocket server = null;
        try {
            // initialize server socket with PORT.
            server = new ServerSocket(PORT);
            System.out.println("Server started.\n Listening for connections on port: " + PORT + "...\n");

            while (true) {
                // we listen until user halts server execution
                Socket connection = server.accept();
                System.out.println("Connection opened. (" + new Date() + ")");
                // Start to run the server logics. connection is for the IO stream.
                runServer(connection);
            }

        } catch (IOException e) {
            System.out.println("Error while accepting request!");
        } finally {
            try {
                // server close connection.
                server.close();

            } catch (IOException e) {
                System.out.println("Unable to close the server socket!");

            }
        }

    }

    private static void runServer(Socket connection) {
        // BufferedReader is a connection for characters (text).
        BufferedReader request = null;
        // BufferedOutputStream is a byte based connection can be
        // used for all types of files (images...)
        BufferedOutputStream response = null;
        try {
            // READ CHARACTERS FROM THE CLIENT VIA INPUT STREAM ON THE SOCKET
            // Initialization for BufferedReader instance.
            request = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            // WE GET character output stream to client (for headers)
            response = new BufferedOutputStream(connection.getOutputStream());

            // Read only the first line of the request, and break it where there is a space
            // character
            String[] fragments = request.readLine().split(" ");
            // method extraction
            String method = fragments[0].toUpperCase();
            // Requested service extraction
            String fileRequested = fragments[1].toLowerCase();

            // Read all headers.
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
            // Read body.
            if (headers.getOrDefault("Content-Type", "Undefined").equals("text/html")) {
                // copy the txt file.
                File file = new File("lib\\TXTCopy.txt");

                if (!file.exists()) {
                    file.createNewFile();
                }

                BufferedWriter writer = new BufferedWriter(new FileWriter("lib\\TXTCopy.txt"));
                String txtLine = "";
                // while(request.ready()){
                while ((txtLine = request.readLine()) != null && txtLine.length() != 0) {

                    // HERE YOU SHOULD WRITE THE FILE LINE BY LINE.

                }

                writer.close();

                requestBody.append("-------------------\n");
                requestBody.append("TXT File Upload Succeed! The path is /");
                requestBody.append(WEB_ROOT);
                requestBody.append(System.getProperty("line.separator"));
            } else {
                try {
                    // while ((line = request.readLine()) != null && line.length() != 0) {
                    while (request.ready()) {
                        line = request.readLine();
                        requestBody.append(line);
                        requestBody.append(System.getProperty("line.separator"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // we support only GET , HEAD and POST methods, we check
            if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
                // If it is not supporting method, use replyMethodNotSupported method.
                replyMethodNotSupported(response, method);

            } else {
                try {
                    // If it is correct method, call the replyWithRequestedFile method.
                    fileRequested = replyWithRequestedFile(response, fileRequested, requestBody.toString(), method);

                } catch (FileNotFoundException fnfe) {
                    try {
                        // If they cannot find the requested file, there will be an exception.
                        // call respondFileNotFound if exception occurs.
                        respondFileNotFound(response, fileRequested, method);

                    } catch (IOException ioe) {
                        System.out.println("Error with file not found exception: " + ioe.getMessage());
                    }
                }
            }

        } catch (IOException ioe) {
            replyServerError(ioe);

        } finally {
            // close Connection
            closeConnection(connection, request, response);
        }
    }

    private static void closeConnection(Socket connect, BufferedReader in, BufferedOutputStream dataOut) {
        // close connection for socket, input reader and output stream.
        // handle exceptions.
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
        // server running error exception
        System.out.println("Server error: " + ioe);
    }

    private static String replyWithRequestedFile(BufferedOutputStream response, String fileRequested,
            String requestBody, String method) throws IOException {
        // Step 1: if what you read is a directory, try to find whether this directory
        // exist,
        // if not there will be filenotfound exception thrown by the program.
        // if it exists, use default file to reply.
        // ADD MORE CONDITIONS FOR SPECIFIC REQUEST, SUCH AS
        // "/uservalidation/", "/pokerdistribution/" AND "/usertextupload/".
        // YOU CAN ADD THE DEFAULT_FILE IN THE RESPONSE AS WELL.

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
                // FILE UPLOAD TO RETURN BACK THE requestBody.

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

        // read content to return to client
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
        out.println(); // blank line between headers and body. VERY IMPORTANT.
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

    // return supported Mime Types
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
