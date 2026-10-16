import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;

public class Depot extends Table{

    public static String tableName = "F_DEPOT";
    public static String configList = "listDepot";
    public static String file ="depot_";

    public static String insert(String filename)
    {
        return  """
                BEGIN TRY  SET DATEFORMAT ymd;
                  DISABLE TRIGGER [TG_INS_F_DEPOT] ON dbo.F_DEPOT;
                 IF OBJECT_ID('F_DEPOT_DEST') IS NOT NULL
                  INSERT INTO [dbo].[F_DEPOT]
                  ([DE_No],[DE_Intitule],[DE_Adresse],[DE_Complement],[DE_CodePostal],[DE_Ville]
                        ,[DE_Contact],[DE_Principal],[DE_CatCompta],[DE_Region],[DE_Pays],[DE_EMail]
                        ,[DE_Code],[DE_Telephone],[DE_Telecopie],[DE_Replication],[DP_NoDefaut],[cbProt]
                        ,[cbCreateur],[cbModification],[cbReplication],[cbFlag],DE_NoSource,cbMarqSource,DatabaseSource)

                  SELECT ISNULL((SELECT Max(DE_No) FROM F_DEPOT),0)  + ROW_NUMBER() OVER(ORDER BY dest.DE_No),dest.[DE_Intitule],[DE_Adresse],[DE_Complement],[DE_CodePostal],[DE_Ville]
                        ,[DE_Contact],[DE_Principal],[DE_CatCompta],[DE_Region],[DE_Pays],[DE_EMail]
                        ,NULL,[DE_Telephone],[DE_Telecopie],[DE_Replication],null/*[DP_NoDefaut]*/,[cbProt]
                        ,[cbCreateur],[cbModification],[cbReplication],[cbFlag],dest.DE_No, dest.cbMarqSource,dest.DatabaseSource
                  FROM F_DEPOT_DEST dest
                  LEFT JOIN (SELECT [DE_NoSource],DataBaseSource,DE_Intitule FROM F_DEPOT) src
                  ON (dest.DE_No = src.DE_NoSource
                  AND dest.DataBaseSource = src.DataBaseSource) OR dest.DE_Intitule = src.DE_Intitule
                  WHERE src.DE_Intitule IS NULL ;

                ENABLE  TRIGGER [TG_INS_F_DEPOT] ON dbo.F_DEPOT;  END TRY
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
                   'F_DEPOT',
                   GETDATE());
                END CATCH\
                """.formatted(filename);
    }

    public static void linkDepot(Connection sqlCon)
    {
        String query = """

                UPDATE dep SET DP_NoDefaut = depE.DP_No
                FROM F_DEPOT dep
                INNER JOIN F_DEPOT_DEST depD
                ON dep.DE_NoSource = depD.DE_No
                AND dep.DataBaseSource = depD.DataBaseSource
                INNER JOIN F_DEPOTEMPL depE
                ON depE.DP_NoSource = depD.DP_NoDefaut
                AND depE.DataBaseSource = depD.DataBaseSource\
                """;
        //                           " UPDATE F_DEPOTEMPL \n" +
        //                           "     SET DE_No = F_DEPOTEMPL_DEST.DE_No \n" +
        //" FROM F_DEPOTEMPL_DEST \n" +
        //" WHERE F_DEPOTEMPL_DEST.DP_No = F_DEPOTEMPL.DP_No";
        executeQuery(sqlCon, query);
    }
    public static void sendDataElement(Connection sqlCon, String path,int unibase)
    {

        File dir = new File(path);
        FilenameFilter filter = (dir1, name) -> name.startsWith(file);
        String[] children = dir.list(filter);
        if (children == null) {
            System.out.println("Either dir does not exist or is not a directory");
        } else {
            for (String filename : children) {
                readOnFile(path, filename, tableName + "_DEST", sqlCon);
                readOnFile(path, "deleteList" + filename, tableName + "_SUPPR", sqlCon);
                updateTableDest(sqlCon, "", "'DE_No','DP_NoDefaut','DE_Code','DE_NoSource','DatabaseSource','DE_Intitule'", tableName, tableName + "_DEST", filename,unibase);
                sendData(sqlCon, path, filename, insert(filename));

                DepotEmpl.sendDataElement(sqlCon, path,unibase);
                linkDepot(sqlCon);

                deleteTempTable(sqlCon, tableName + "_DEST");

                deleteDepot(sqlCon, path, filename);
            }
        }

    }
    public static void getDataElement(Connection sqlCon, String path,String database,String time)
    {
        String filename =  file+time+".csv";
        initTableParam(sqlCon,tableName,configList,"DE_No,DatabaseSource");
        getData(sqlCon, selectSourceTable(tableName,database,true), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);

        DepotEmpl.getDataElement(sqlCon, path,database, time);
    }
    public static void getDataElementFilterAgency(Connection sqlCon, String path,String database,String time,String agency)
    {
        String filename =  file+time+".csv";
        initTableParam(sqlCon,tableName,configList,"DE_No,DatabaseSource");
        getData(sqlCon, selectSourceTableFilterAgency(tableName,database,agency,"DE_No,DatabaseSource"), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);

        DepotEmpl.getDataElement(sqlCon, path,database, time);
    }
    public static void deleteDepot(Connection sqlCon, String path,String filename)
    {
        String query =
                """
                         DELETE FROM F_DEPOT
                         WHERE EXISTS (SELECT 1 FROM F_DEPOT_SUPPR WHERE ISNULL(F_DEPOT_SUPPR.DE_No,0) = ISNULL(F_DEPOT.DE_NoSource,0) AND ISNULL(F_DEPOT.DataBaseSource,'') = ISNULL(F_DEPOT_SUPPR.DataBaseSource,'')   )

                         IF OBJECT_ID('F_DEPOT_SUPPR') IS NOT NULL
                         DROP TABLE F_DEPOT_SUPPR ;\
                        """;
        if ((new File(path + "\\deleteList" + filename)).exists())
        {
            executeQuery(sqlCon, query);
            archiveDocument(path + "\\archive", path, "deleteList" + filename);
        }
    }
}
