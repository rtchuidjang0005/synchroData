import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;

public class EcritureA extends Table {

    public static String file = "EcritureA_";
    public static String dbSource = "BIJOU";
    public static String tableName = "F_ECRITUREA";
    public static String configList = "listEcritureA";

    public static String list()
    {
        return   """
                SELECT    [EC_No],[N_Analytique],[EA_Ligne],[CA_Num]
                        ,[EA_Montant],[EA_Quantite],[cbProt],[cbMarq]
                        ,[cbCreateur],[cbModification],[cbReplication],[cbFlag]
                        ,cbMarqSource = [cbMarq]
                        ,[DataBaseSource] = '%s'
                FROM    [F_ECRITUREA]WHERE cbModification >= ISNULL((SELECT LastSynchro FROM config.SelectTable WHERE tableName='F_ECRITUREA'),'1900-01-01')\
                """.formatted(dbSource);
    }

    public static String insert(String filename)
    {
        return  """
                BEGIN TRY
                 IF OBJECT_ID('F_ECRITUREA_DEST') IS NOT NULL
                INSERT INTO F_ECRITUREA ([EC_No],[N_Analytique],[EA_Ligne],[CA_Num]
                        ,[EA_Montant],[EA_Quantite],[cbProt]
                        ,[cbCreateur],[cbModification],[cbReplication],[cbFlag]
                        ,[DataBaseSource],cbMarqSource)
                SELECT    ecr.[EC_No],[N_Analytique],[EA_Ligne],[CA_Num]
                        ,[EA_Montant],[EA_Quantite],[cbProt]
                        ,[cbCreateur],[cbModification],[cbReplication],[cbFlag]
                        ,dest.[DataBaseSource],dest.cbMarqSource
                FROM    [F_ECRITUREA_DEST] dest
                LEFT JOIN (SELECT cbMarqSource,DataBaseSource FROM F_ECRITUREA) src
                    ON    ISNULL(src.cbMarqSource,0) = ISNULL(dest.cbMarqSource,0)
                    AND        ISNULL(src.DatabaseSource,'') = ISNULL(dest.DatabaseSource,'')
                LEFT JOIN (SELECT EC_No,EC_NoSource,DatabaseSource FROM F_ECRITUREC) ecr
                    ON    ISNULL(ecr.EC_NoSource,0) = ISNULL(dest.EC_No,0)
                    AND ISNULL(ecr.DataBaseSource,'') = ISNULL(dest.DataBaseSource,'')
                WHERE ecr.EC_No IS NOT NULL

                AND src.cbMarqSource IS NULL
                 END TRY
                 BEGIN CATCH
                INSERT INTO config.DB_Errors
                    VALUES
                  (SUSER_SNAME(),
                   ERROR_NUMBER(),
                   ERROR_STATE(),
                   ERROR_SEVERITY(),
                   ERROR_LINE(),
                   ERROR_PROCEDURE(),
                   ERROR_MESSAGE(),
                   'Insert '+ ' %s',
                   'F_ECRITUREA',
                   GETDATE());
                END CATCH\
                """.formatted(filename);
    }

    public static void sendDataElement(Connection  sqlCon, String path,String database,int unibase)
    {
        File dir = new File(path);
        FilenameFilter filter = (dir1, name) -> name.startsWith(file);
        String[] children = dir.list(filter);
        if (children == null) {
            System.out.println("Either dir does not exist or is not a directory");
        } else {
            for (String filename : children) {
                dbSource = database;
                readOnFile(path, filename, tableName + "_DEST", sqlCon);
                readOnFile(path, "deleteList" + filename, tableName + "_SUPPR", sqlCon);
                updateTableDest(sqlCon, "", "'EC_No','N_Analytique'", tableName, tableName + "_DEST", filename,unibase);
                sendData(sqlCon, path, filename, insert(filename));
                deleteTempTable(sqlCon, tableName + "_DEST");
                deleteEcritureA(sqlCon, path, filename);
            }
        }
    }
    public static void getDataElement(Connection  sqlCon, String path,String database,String time)
    {
        String filename =  file+time+".csv";
        dbSource = database;
        initTableParam(sqlCon,tableName,configList,"EC_No,DataBaseSource");
        getData(sqlCon, selectSourceTable(tableName,database,true), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);

    }
    public static void deleteEcritureA(Connection sqlCon, String path,String filename)
    {
        String query =
                """
                         DELETE FROM F_ECRITUREA
                         WHERE EXISTS (SELECT 1 FROM F_ECRITUREA_SUPPR WHERE F_ECRITUREA_SUPPR.EC_No = F_ECRITUREA.EC_No)

                         IF OBJECT_ID('F_ECRITUREA_SUPPR') IS NOT NULL
                         DROP TABLE F_ECRITUREA_SUPPR ;\
                        """;
        if ((new File(path + "\\deleteList" + filename)).exists())
        {
            executeQuery(sqlCon, query);
            archiveDocument(path + "\\archive", path, "deleteList" + filename);
        }
    }

    public static void listDeleteEcritureA(Connection sqlCon, String path)
    {
        String query = " SELECT lart.EC_No,lart.cbMarq " +
                " FROM config.ListEcritureA lart " +
                " LEFT JOIN dbo.F_ECRITUREA fart " +
                "    ON lart.cbMarq = fart.cbMarq " +
                " WHERE fart.cbMarq IS NULL " +
                ";";

        writeToFileAvro(path + "\\deleteList" + file, query, sqlCon);
        writeOnFile(path + "\\deleteList" + file, query, sqlCon);

        query = " DELETE FROM config.ListEcritureA " +
                " WHERE NOT EXISTS(SELECT   1 " +
                "                  FROM     F_ECRITUREA " +
                "                  WHERE    dbo.F_ECRITUREA.cbMarq = config.ListEcritureA.cbMarq);";
        executeQuery(sqlCon, query);
    }
}
