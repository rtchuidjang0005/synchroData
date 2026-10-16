import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UpdateData {

    public static void unzip(File path,File source){

        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(path.getAbsolutePath()));
            ZipEntry entry = null;
            while((entry = zis.getNextEntry()) != null){
                int len;
                byte[] data = new byte[1024];
                FileOutputStream fos = new FileOutputStream(source.getPath()+"\\"+entry.getName());
                while((len = zis.read(data))!= -1){
                    fos.write(data,0,len);
                }
                fos.close();
                zis.closeEntry();
            }
            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        String databaseDestFile = "resource/databaseSource.json";
        if(args.length > 0)
            databaseDestFile = args[0];

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        JSONArray listObject = DataBase.getInfoConnexion(databaseDestFile);
        if (listObject != null) {
            for (Object o : listObject) {
                JSONObject list = (JSONObject) o;

                if (list.get("reception").equals("1")) {
                    int unibase = 0;
                    String path = ((String) list.get("path"));
                    FileFilter zipFileFilter = (file) -> {
                        return file.getName().endsWith(".zip");
                    };

                    File[] files = (new File(path)).listFiles(zipFileFilter);
                    File sourcePath = new File(path);
                    for(File file : files) {
                        unzip(file, sourcePath);
                        file.delete();
                    }
                    try {
                        String dbURL = "jdbc:sqlserver://" + list.get("servername") + ";databaseName=" + list.get("database");
                        Properties properties = new Properties();
                        properties.put("user", list.get("username"));
                        properties.put("password", list.get("password"));
                        Connection sqlCon = DriverManager.getConnection(dbURL, properties);
                        sqlCon.setAutoCommit(true);
                        System.out.println("--Démarrage Application--");
                        System.out.println("Base de données : " + list.get("database") + "\n Chemin : " + path + "\n");
                        System.out.println("Serveur : " + list.get("servername"));
                        String database = (String) list.get("database");
                        Table.setUpdateExistingConfig(list.get("updateExisting"));
    //            DocLigne.sendDataElement(sqlCon, path,list.get(1));

                        Object valueSelect = list.get("compteg");
                        if ((list.get("compteg")).equals("1")) {
                            System.out.println("--Chargement Compte Général--");
                            Compteg.sendDataElement(sqlCon, path,unibase);
                        }
                        if ((list.get("taxe")).equals("1")) {
                            System.out.println("--Chargement Taxe--");
                            Taxe.sendDataElement(sqlCon, path,unibase);
                        }
                        if ((list.get("collaborateur")).equals("1")) {
                            System.out.println("--Chargement Collaborateur--");
                            Collaborateur.sendDataElement(sqlCon, path,unibase);
                        }
                        if ((list.get("journaux")).equals("1")) {
                            System.out.println("--Chargement Journaux--");
                            FJournaux.sendDataElement(sqlCon, path,unibase);
                        }
                        if ((list.get("depot")).equals("1")) {
                            System.out.println("--Chargement Depot--");
                            Depot.sendDataElement(sqlCon, path,unibase);
                        }

                        if ((list.get("tiers")).equals("1")) {
                            System.out.println("--Chargement Compte Tiers--");
                            Comptet.sendDataElement(sqlCon, path,unibase);
                            System.out.println("--Chargement Livraison--");
                            Livraison.sendDataElement(sqlCon, path,unibase);
                        }

                        if ((list.get("article")).equals("1")) {
                            System.out.println("--Chargement Famille--");
                            Famille.sendDataElement(sqlCon, path,unibase);
                            System.out.println("--Chargement Article--");
                            Article.sendDataElement(sqlCon, path,unibase);
                        }

                        if ((list.get("entete")).equals("1")) {
                            System.out.println("--Chargement Caisse--");
                            Caisse.sendDataElement(sqlCon, path, database,unibase);
                            System.out.println("--Chargement Entete--");
                            DocEntete.sendDataElement(sqlCon, path, database,unibase);
                            System.out.println("--Chargement DocRegl--");
                            DocRegl.sendDataElement(sqlCon, path, database,unibase);
                        }

                        if ((list.get("ligne")).equals("1")) {
                            System.out.println("--Chargement Ligne--");
                            DocLigne.sendDataElement(sqlCon, path, database,unibase);
                        }

                        if ((list.get("reglement")).equals("1")) {
                            System.out.println("--Chargement Reglement--");
                            Reglement.sendDataElement(sqlCon, path, database,unibase);
                            System.out.println("--Chargement ReglEch--");
                            ReglEch.sendDataElement(sqlCon, path, database,unibase);
                        }

                        if ((list.get("artstock")).equals("1")) {
                            System.out.println("--Chargement ArtStock--");
                            ArtStock.sendDataElement(sqlCon, path, database,unibase);
                        }

                        if (list.get("ecriturec").equals("1")) {
                            System.out.println("--Chargement JMouv--");
                            JMouv.sendDataElement(sqlCon, path,unibase);

                            System.out.println("--Chargement Ecriture Comptable--");
                            EcritureC.sendDataElement(sqlCon, path, database,unibase);
                        }

                        if (list.get("ecriturea").equals("1")) {
                            System.out.println("--Chargement Ecriture Analytique--");
                            EcritureA.sendDataElement(sqlCon, path, database,unibase);
                        }

                        valueSelect = list.get("fenumcond");
                        if (valueSelect != null && valueSelect.equals("1")) {
                            System.out.println("--Chargement Enumere Cond--");
                            FEnumCond.sendDataElement(sqlCon, path, unibase);
                        }

                        valueSelect = list.get("ftarifcond");
                        if (valueSelect != null && valueSelect.equals("1")) {
                            System.out.println("--Chargement Tarif Conditionne --");
                            FTarifCond.sendDataElement(sqlCon, path, unibase);
                        }

                        valueSelect = list.get("pconditionnement");
                        if (valueSelect != null && valueSelect.equals("1")) {
                            System.out.println("--Chargement PConditionnement--");
                            PConditionnement.sendDataElement(sqlCon, path,unibase);
                        }

                        valueSelect = list.get("punite");
                        if (valueSelect != null && valueSelect.equals("1")) {
                            System.out.println("--Chargement PUnite--");
                            PUnite.sendDataElement(sqlCon, path,unibase);
                        }

                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }
        }
    }
}
