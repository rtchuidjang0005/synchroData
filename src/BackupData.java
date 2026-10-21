import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupData {

    public static void zip(File path,String creationDate){

        FileFilter csvFileFilter = (file) -> {
            return file.getName().endsWith(".avro");
        };

        File[] files = path.listFiles(csvFileFilter);

        if(files.length == 0 )
            throw new IllegalArgumentException("No files in path"+path.getAbsolutePath());

        try {
            FileOutputStream fos = new FileOutputStream(path.getAbsolutePath() + "/file_"+creationDate+".zip");
            ZipOutputStream zos = new ZipOutputStream(fos);
            for(File zipThis : files){
                FileInputStream fis = new FileInputStream(zipThis);
                ZipEntry zipEntry = new ZipEntry(zipThis.getName());
                zos.putNextEntry(zipEntry);
                byte[] bytes = new byte[2048];
                int length;
                while((length = fis.read(bytes)) >= 0){
                    zos.write(bytes,0,length);
                }
                fis.close();
            }
            zos.close();
            fos.close();

            for(File file : files){
                file.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        String databaseSourceFile = "resource/databaseSource.json";
        if(args.length > 0)
            databaseSourceFile = args[0];
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        JSONArray listObject = DataBase.getInfoConnexion(databaseSourceFile);
        if (listObject != null) {
            for (Object o : listObject) {
                JSONObject list = (JSONObject) o;

                if (list.get("envoi").equals("1") && list.get("active").equals("1")) {
                    String path = (String) list.get("path");
                    try {
                        String dbURL = Table.jdbcUrl(list.get("servername"), list.get("database"));
                        Properties properties = new Properties();
                        properties.put("user", list.get("username"));
                        properties.put("password", list.get("password"));
                        Connection sqlCon = DriverManager.getConnection(dbURL, properties);
                        sqlCon.setAutoCommit(true);

    //            ReglEch.getDataElement(sqlCon, path,list.get(1));
                        String database = (String) list.get("database");
                        Object valueSelect = list.get("taxe");

                        valueSelect = list.get("taxe");
                        if (valueSelect != null && valueSelect.equals("1")) {
                            System.out.println("--Sauvegarde taxe--");
                            Taxe.getDataElement(sqlCon, path, database, simpleDateFormat.format(new Date()));
                        }
                        valueSelect = list.get("depot");
                        if (valueSelect != null && valueSelect.equals("1")) {
                            System.out.println("--Sauvegarde depot--");
                            Depot.getDataElement(sqlCon, path, database, simpleDateFormat.format(new Date()));
                        }
                        valueSelect = list.get("tiers");
                        if (valueSelect != null && valueSelect.equals("1")) {
                            System.out.println("--Sauvegarde tiers--");
                            Comptet.getDataElement(sqlCon, path, database, simpleDateFormat.format(new Date()));
                            System.out.println("--Sauvegarde livraison--");
                            Livraison.getDataElement(sqlCon, path, database, simpleDateFormat.format(new Date()));
                        }
                        valueSelect = list.get("article");
                        if (valueSelect != null && valueSelect.equals("1")) {
                            System.out.println("--Sauvegarde famille--");
                            Famille.getDataElement(sqlCon, path, database, simpleDateFormat.format(new Date()));
                            System.out.println("--Sauvegarde article--");
                            Article.getDataElement(sqlCon, path, database, simpleDateFormat.format(new Date()));
                        }
                        valueSelect = list.get("entete");
                        if (valueSelect != null && valueSelect.equals("1")) {
                            System.out.println("--Sauvegarde Caisse--");
                            Caisse.getDataElement(sqlCon, path, database, simpleDateFormat.format(new Date()));
                            System.out.println("--Sauvegarde entete--");
                            DocEntete.getDataElement(sqlCon, path, database, simpleDateFormat.format(new Date()), (JSONObject) list.get("typedocument"));
                            System.out.println("--Sauvegarde DocRegl--");
                            DocRegl.getDataElement(sqlCon, path, database, simpleDateFormat.format(new Date()), (JSONObject) list.get("typedocument"));
                        }
                        valueSelect = list.get("ligne");
                        if (valueSelect != null && valueSelect.equals("1")) {
                            System.out.println("--Sauvegarde ligne--");
                            DocLigne.getDataElement(sqlCon, path, database, simpleDateFormat.format(new Date()), (JSONObject) list.get("typedocument"));

                        }
                        valueSelect = list.get("reglement");
                        if (valueSelect != null && valueSelect.equals("1")) {
                            System.out.println("--Sauvegarde ReglEch--");
                            ReglEch.getDataElement(sqlCon, path, database, simpleDateFormat.format(new Date()), (JSONObject) list.get("typedocument"));
                            System.out.println("--Sauvegarde Reglement--");
                            Reglement.getDataElement(sqlCon, path, database, simpleDateFormat.format(new Date()));
                        }
                        valueSelect = list.get("artstock");
                        if (valueSelect != null && valueSelect.equals("1")) {
                            System.out.println("--Sauvegarde ArtStock--");
                            ArtStock.getDataElement(sqlCon, path, database, simpleDateFormat.format(new Date()));
                        }
                        valueSelect = list.get("ecriturec");
                        if (valueSelect != null && valueSelect.equals("1")) {
                            System.out.println("--Sauvegarde JMouv--");
                            JMouv.getDataElement(sqlCon, path, database, simpleDateFormat.format(new Date()));
                            System.out.println("--Sauvegarde EcritureC--");
                            EcritureC.getDataElement(sqlCon, path, database, simpleDateFormat.format(new Date()));
                        }
                        valueSelect = list.get("ecriturea");
                        if (valueSelect != null && valueSelect.equals("1")) {
                            System.out.println("--Sauvegarde EcritureA--");
                            EcritureA.getDataElement(sqlCon, path, database, simpleDateFormat.format(new Date()));
                        }
                        valueSelect = list.get("compteg");
                        if (valueSelect != null && valueSelect.equals("1")) {
                            System.out.println("--Sauvegarde CompteG--");
                            Compteg.getDataElement(sqlCon, path, database, simpleDateFormat.format(new Date()));
                        }
                        valueSelect = list.get("journaux");
                        if (valueSelect != null && valueSelect.equals("1")) {
                            System.out.println("--Sauvegarde Journaux--");
                            FJournaux.getDataElement(sqlCon, path, database, simpleDateFormat.format(new Date()));
                        }
                        valueSelect = list.get("collaborateur");
                        if (valueSelect != null && valueSelect.equals("1")) {
                            System.out.println("--Sauvegarde Collaborateur--");
                            Collaborateur.getDataElement(sqlCon, path, database, simpleDateFormat.format(new Date()));
                        }

                        valueSelect = list.get("fenumcond");
                        if (valueSelect != null && valueSelect.equals("1")) {
                            System.out.println("--Sauvegarde Enumere Condition--");
                            FEnumCond.getDataElement(sqlCon, path, database, simpleDateFormat.format(new Date()));
                        }
                        valueSelect = list.get("ftarifcond");
                        if (valueSelect != null && valueSelect.equals("1")) {
                            System.out.println("--Sauvegarde Tarif Condition--");
                            FTarifCond.getDataElement(sqlCon, path, database, simpleDateFormat.format(new Date()));
                        }
                        valueSelect = list.get("pconditionnement");
                        if (valueSelect != null && valueSelect.equals("1")) {
                            System.out.println("--Sauvegarde PConditionnement--");
                            PConditionnement.getDataElement(sqlCon, path, database, simpleDateFormat.format(new Date()));
                        }
                        valueSelect = list.get("punite");
                        if (valueSelect != null && valueSelect.equals("1")) {
                            System.out.println("--Sauvegarde PUnite--");
                            PUnite.getDataElement(sqlCon, path, database, simpleDateFormat.format(new Date()));
                        }

                        zip(new File((String) list.get("path")),simpleDateFormat.format(new Date()));
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }
        }
    }
}
