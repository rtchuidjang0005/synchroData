import com.jcraft.jsch.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.sql.*;
import java.util.Properties;
import java.util.Vector;

public class UpdateDatabase {

    public static void executeSQL(File f, Connection c) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(f));
        StringBuilder sql = new StringBuilder();
        String line;

        // Read the entire SQL file into the StringBuilder
        while ((line = br.readLine()) != null) sql.append(line).append("\n");

        br.close(); // Close the BufferedReader after reading the file

        // Split the SQL script by the "GO" keyword (which can have leading/trailing spaces)
        String[] sqlStatements = sql.toString().split("(?i)\\bGO\\b");

        try {
            Statement stmt = c.createStatement();

            // Execute each statement individually
            for (String statement : sqlStatements) {
                if (statement.trim().isEmpty()) continue;  // Skip empty statements
                stmt.execute(statement);
            }

            stmt.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void deleteFolderContent(String path){
        File dossierParent = new File(path); // Remplacer par le chemin du dossier
        File archive = new File(dossierParent, "archive"); // Spécifier le dossier archive

        // Première installation : le répertoire de travail de la configuration n'existe pas encore
        if (!dossierParent.exists()) {
            if (dossierParent.mkdirs()) {
                System.out.println("Répertoire de travail créé : " + dossierParent.getAbsolutePath());
            } else {
                System.out.println("Impossible de créer le répertoire de travail : " + dossierParent.getAbsolutePath());
            }
            return; // dossier neuf : rien à vider
        }

        supprimerContenu(dossierParent, archive);
    }

    public static void supprimerContenu(File dossier, File exclusion) {
        if (dossier.isDirectory()) {
            File[] fichiers = dossier.listFiles();

            if (fichiers != null) {
                for (File fichier : fichiers) {
                    // Ne pas supprimer le dossier d'archive ni ses sous-dossiers
                    if (!fichier.equals(exclusion)) {
                        supprimer(fichier);
                    }
                }
            }
        }
    }

    public static void supprimer(File fichier) {
        if (fichier.isDirectory()) {
            File[] enfants = fichier.listFiles();

            if (enfants != null) {
                for (File enfant : enfants) {
                    supprimer(enfant);
                }
            }
        }

        // Supprimer le fichier ou le dossier vide
        fichier.delete();
    }

    public static void deleteContentFtp(String path,String folderftp) {
        JSch jsch = new JSch();
        Session session;
        try {
            //session = jsch.getSession("u85460117-upload", "home631778145.1and1-data.host", 22);
            session = jsch.getSession("u94723657", "home751918776.1and1-data.host", 22);
            session.setPassword("ilIBWTvZme4CZD5BzMez");
            //session.setPassword("FyK6cIAgpeNOSbEdfrpC*");
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            File dir = new File((String) path);

            File[] directoryListing = dir.listFiles();

            // Supprimer le contenu du répertoire distant
            supprimerContenuSFTP(sftpChannel, "/" + folderftp);

            sftpChannel.exit();
            session.disconnect();
        } catch (JSchException | SftpException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour supprimer récursivement le contenu d'un répertoire sur SFTP
    public static void supprimerContenuSFTP(ChannelSftp sftpChannel, String dossierPath) throws SftpException {
        // Lister le contenu du dossier distant
        @SuppressWarnings("unchecked")
        Vector<ChannelSftp.LsEntry> fichiers = sftpChannel.ls(dossierPath);

        for (ChannelSftp.LsEntry fichier : fichiers) {
            String nomFichier = fichier.getFilename();

            // Ignorer les liens symboliques "." et ".."
            if (nomFichier.equals(".") || nomFichier.equals("..")) {
                continue;
            }

            String cheminComplet = dossierPath + "/" + nomFichier;

            // Si c'est un fichier, le supprimer
            if (fichier.getAttrs().isReg()) {
                sftpChannel.rm(cheminComplet);
                System.out.println("Fichier supprimé : " + cheminComplet);
            }

            // Si c'est un dossier, le supprimer récursivement
            if (fichier.getAttrs().isDir()) {
                supprimerContenuSFTP(sftpChannel, cheminComplet);
                sftpChannel.rmdir(cheminComplet); // Supprimer le dossier après avoir supprimé son contenu
                System.out.println("Dossier supprimé : " + cheminComplet);
            }
        }
    }

    public static void main(String[] args) {

        String databaseSourceFile = "resource/databaseSource.json";
        if (args.length > 0)
            databaseSourceFile = args[0];

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        String dateSynchro = "1900-01-01";
        JSONArray listObject = DataBase.getInfoConnexion(databaseSourceFile);
        if (listObject != null) {
            for (Object o : listObject) {
                JSONObject list = (JSONObject) o;
                if (list.get("reception").equals("1") && list.get("active").equals("1")) {
                    try {
                        String dbURL = Table.jdbcUrl(list.get("servername"), list.get("database"));
                        Properties properties = new Properties();
                        properties.put("user", list.get("username"));
                        properties.put("password", list.get("password"));
                        Connection sqlCon = DriverManager.getConnection(dbURL, properties);

                        Object valueSelect = list.get("datemaj");
                        if (valueSelect != null && valueSelect.equals("1"))
                            dateSynchro = (String) list.get("datemaj");

                        executeSQL(new File("resource/configListTable.sql"), sqlCon);
                        executeSQL(new File("resource/updateBdd.sql"), sqlCon);
                        PreparedStatement pstmt = null;
                        String sql = "UPDATE config.selectTable SET lastSynchro = ?";
                        deleteFolderContent((String) list.get("path"));

                        deleteContentFtp((String) list.get("path"),(String)list.get("folderftp"));
                        try {
                                pstmt = sqlCon.prepareStatement(sql);
                                pstmt.setString(1, dateSynchro); // Assuming dateSynchro is already in the correct format
                                pstmt.executeUpdate();
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        } finally {
                            if (pstmt != null) {
                                try {
                                    pstmt.close();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (Exception throwables) {
                        throwables.printStackTrace();
                    }
                }
            }
        }
    }
}
