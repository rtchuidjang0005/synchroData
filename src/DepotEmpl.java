import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;

public class DepotEmpl extends Table {

    public static String file = "depotEmpl_";
    public static String tableName = "F_DEPOTEMPL";
    public static String configList = "listDepotEmpl";

    public static String insert(String filename)
    {
        return          """
                BEGIN TRY  SET DATEFORMAT ymd;
                 IF OBJECT_ID('F_DEPOTEMPL_DEST') IS NOT NULL AND {{UPDATE_ENABLED}} = 1
                 UPDATE [dbo].[F_DEPOTEMPL]
                   SET [DP_Code] = F_DEPOTEMPL_DEST.DP_Code
                      ,[DP_Intitule] = F_DEPOTEMPL_DEST.DP_Intitule
                      ,[DP_Zone] = F_DEPOTEMPL_DEST.DP_Zone
                      ,[DP_Type] = F_DEPOTEMPL_DEST.DP_Type
                      ,[cbProt] = F_DEPOTEMPL_DEST.cbProt
                      ,[cbCreateur] = F_DEPOTEMPL_DEST.cbCreateur
                      ,[cbModification] = F_DEPOTEMPL_DEST.cbModification
                      ,[cbReplication] = F_DEPOTEMPL_DEST.cbReplication
                      ,[cbFlag] = F_DEPOTEMPL_DEST.cbFlag
                FROM F_DEPOTEMPL_DEST
                  WHERE ISNULL(F_DEPOTEMPL.DP_NoSource,0) = ISNULL(F_DEPOTEMPL_DEST.DP_No,0)
                  AND    ISNULL(F_DEPOTEMPL.DataBaseSource,'') = ISNULL(F_DEPOTEMPL_DEST.DataBaseSource,'')

                  INSERT INTO [dbo].[F_DEPOTEMPL]
                  ([DP_No],[DE_No],[DP_Code],[DP_Intitule],[DP_Zone],[DP_Type],[cbProt]
                      ,[cbCreateur],[cbModification],[cbReplication],[cbFlag],DP_NoSource,cbMarqSource,DataBaseSource)

                  SELECT ISNULL((SELECT Max(DP_No) FROM F_DEPOTEMPL),0)  + ROW_NUMBER() OVER(ORDER BY dest.DP_No),srcDep.[DE_No],[DP_Code],[DP_Intitule],[DP_Zone],[DP_Type],[cbProt]
                      ,[cbCreateur],[cbModification],[cbReplication],[cbFlag],dest.DP_No,dest.cbMarqSource,dest.DataBaseSource
                  FROM F_DEPOTEMPL_DEST dest
                  LEFT JOIN (SELECT DP_NoSource,DataBaseSource FROM F_DEPOTEMPL) src
                  ON ISNULL(dest.DP_No,0) = ISNULL(src.DP_NoSource,0)
                  AND ISNULL(dest.DataBaseSource,'') = ISNULL(src.DataBaseSource,'')
                  LEFT JOIN (SELECT DatabaseSource,DE_NoSource,DE_No FROM F_DEPOT) srcDep
                  ON    ISNULL(dest.DE_No,0) = ISNULL(srcDep.DE_NoSource,0)
                  AND    ISNULL(dest.DataBaseSource,'') = ISNULL(srcDep.DataBaseSource,'')
                  WHERE src.DP_NoSource IS NULL
                  AND    srcDep.DE_No IS NOT NULL;     IF OBJECT_ID('F_DEPOTEMPL_DEST') IS NOT NULL
                  DROP TABLE F_DEPOTEMPL_DEST; END TRY
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
                   'F_DEPOTEMPL',
                   GETDATE());
                END CATCH\
                """.formatted(filename);
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
                updateTableDest(sqlCon, "", "'DP_No','DE_No','DP_NoSource'", tableName, tableName + "_DEST", filename,unibase);
                sendData(sqlCon, path, filename, applyUpdateFlag(insert(filename), tableName));

                deleteTempTable(sqlCon, tableName + "_DEST");
                deleteDepot(sqlCon, path, filename);
            }
        }
    }
    public static void getDataElement(Connection sqlCon, String path,String database,String time)
    {
        String filename =  file+time+".csv";
        initTableParam(sqlCon,tableName,configList,"DP_No,DatabaseSource");
        getData(sqlCon, selectSourceTable(tableName,database,true), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);
    }
    public static void deleteDepot(Connection sqlCon, String path,String filename)
    {
        String query =
                """
                         DELETE FROM F_DEPOTEMPL
                         WHERE EXISTS (SELECT 1                FROM F_DEPOTEMPL_SUPPR emplSuppr               WHERE ISNULL(emplSuppr.DP_No,0) = ISNULL(F_DEPOTEMPL.DP_NoSource,0)                AND ISNULL(emplSuppr.DataBaseSource,'') = ISNULL(F_DEPOTEMPL.DataBaseSource,'')   )

                         IF OBJECT_ID('F_DEPOTEMPL_SUPPR') IS NOT NULL
                         DROP TABLE F_DEPOTEMPL_SUPPR ;\
                        """;
        if ((new File(path + "\\deleteList" + filename)).exists())
        {
            executeQuery(sqlCon, query);
            archiveDocument(path + "\\archive", path, "deleteList" + filename);
        }
    }
}
