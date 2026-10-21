import org.json.simple.JSONObject;

import java.sql.Connection;

public class ReglEch extends Table {

    public static String file = "ReglEch_";
    public static String dbSource = "BIJOU";
    public static String tableName = "F_REGLECH";
    public static String configList = "listReglEch";

    public static String insert(String filename)
    {
        return """
                BEGIN TRY  SET DATEFORMAT ymd;
                 IF OBJECT_ID('F_REGLECH_DEST') IS NOT NULL
                BEGIN
                IF OBJECT_ID('tempdb..#ReglEch') IS NOT NULL
                    DROP TABLE #ReglEch

                SELECT     cre.[RG_No],fdr.[DR_No],dest.[DO_Domaine],dest.[DO_Type]
                        ,dest.[DO_Piece],dest.[RC_Montant],dest.[RG_TypeReg],dest.[cbProt],dest.[cbCreateur]
                        ,dest.[cbModification],dest.[cbReplication],dest.[cbFlag],dest.cbMarqSource,dest.DataBaseSource
                        ,[DR_NoSource] = dest.DR_No,[RG_NoSource] = dest.RG_No
                INTO #ReglEch
                FROM F_REGLECH_DEST dest
                LEFT JOIN (SELECT DataBaseSource,cbMarqSource FROM F_REGLECH) src
                    ON    ISNULL(dest.DataBaseSource,'') = ISNULL(src.DataBaseSource,'')
                    AND ISNULL(dest.cbMarqSource,0) = ISNULL(src.cbMarqSource,0)
                LEFT JOIN F_DOCREGL fdr
                    ON    ISNULL(fdr.DataBaseSource,'') = ISNULL(dest.DataBaseSource,'')
                    AND ISNULL(fdr.DR_NoSource,0) = ISNULL(dest.DR_No,0)
                LEFT JOIN F_CREGLEMENT cre
                    ON    ISNULL(cre.DataBaseSource,'') = ISNULL(dest.DataBaseSource,'')
                    AND ISNULL(cre.RG_NoSource,0) = ISNULL(dest.RG_No,0)
                WHERE src.cbMarqSource IS NULL
                AND cre.RG_No IS NOT NULL AND fdr.DR_No IS NOT NULL

                {{FK_CHECK}}

                INSERT INTO F_REGLECH (
                [RG_No],[DR_No],[DO_Domaine],[DO_Type]
                        ,[DO_Piece],[RC_Montant],[RG_TypeReg],[cbProt],[cbCreateur]
                        ,[cbModification],[cbReplication],[cbFlag],cbMarqSource,DataBaseSource,DR_NoSource,RG_NoSource)
                SELECT [RG_No],[DR_No],[DO_Domaine],[DO_Type]
                        ,[DO_Piece],[RC_Montant],[RG_TypeReg],[cbProt],[cbCreateur]
                        ,[cbModification],[cbReplication],[cbFlag],cbMarqSource,DataBaseSource,DR_NoSource,RG_NoSource
                FROM #ReglEch
                END

                INSERT INTO config.DB_Errors(
                          UserName,
                          ErrorNumber,
                          ErrorState,
                          ErrorSeverity,
                          ErrorLine,
                          ErrorProcedure,
                          ErrorMessage,
                          TableLoad,
                          Query,
                          ErrorDateTime)
                SELECT       SUSER_SNAME(),           NULL,           NULL,           NULL,           NULL,           NULL,           NULL,'Domaine : '+CAST(dest.[DO_Domaine] AS VARCHAR(150))+' Type : ' + CAST(dest.[DO_Type] AS VARCHAR(150)) + ' Piece : ' + CAST(dest.[DO_Piece] AS VARCHAR(150))           +' cbMarq : ' + CAST(dest.[cbMarqSource] AS VARCHAR(150)) + ' database : ' + CAST(dest.[DataBaseSource] AS VARCHAR(150))           + 'fileName : %s RG_No : '+CAST(dest.RG_No AS VARCHAR(150))+' DR_No : '+CAST(dest.DR_No AS VARCHAR(150))             ,'F_REGLECH'
                           ,GETDATE()
                FROM F_REGLECH_DEST dest
                LEFT JOIN (SELECT DataBaseSource,cbMarqSource FROM F_REGLECH) src
                    ON    ISNULL(dest.DataBaseSource,'') = ISNULL(src.DataBaseSource,'')
                    AND ISNULL(dest.cbMarqSource,0) = ISNULL(src.cbMarqSource,0)
                LEFT JOIN F_DOCREGL fdr
                    ON    ISNULL(fdr.DataBaseSource,'') = ISNULL(dest.DataBaseSource,'')
                    AND ISNULL(fdr.DR_NoSource,0) = ISNULL(dest.DR_No,0)
                LEFT JOIN F_CREGLEMENT cre
                    ON    ISNULL(cre.DataBaseSource,'') = ISNULL(dest.DataBaseSource,'')
                    AND ISNULL(cre.RG_NoSource,0) = ISNULL(dest.RG_No,0)
                WHERE src.cbMarqSource IS NULL
                AND (cre.RG_No IS NULL OR fdr.DR_No IS NULL)

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
                   'Insert %s',
                   'F_REGLECH',
                   GETDATE());
                END CATCH\
                """.formatted(filename, filename)
                .replace("{{FK_CHECK}}", foreignKeyCheck(tableName, "#ReglEch",
                        "N'Domaine : ' + CAST(t.DO_Domaine AS NVARCHAR(10)) + N' Type : ' + CAST(t.DO_Type AS NVARCHAR(10))"
                                + " + N' Piece : ' + t.DO_Piece + N' RG_No source : ' + CAST(t.RG_NoSource AS NVARCHAR(20))"
                                + " + N' DR_No source : ' + CAST(t.DR_NoSource AS NVARCHAR(20))"
                                + " + N' cbMarq : ' + CAST(t.cbMarqSource AS NVARCHAR(20)) + N' database : ' + ISNULL(t.DataBaseSource, N'')",
                        filename));
    }

    public static void sendDataElement(Connection sqlCon, String path,String database,int unibase)
    {
        dbSource = database;
        loadFile(path,sqlCon,unibase);
        loadDeleteFile(path,sqlCon);
        DocEntete.loadDeleteFile(path,sqlCon);
    }

    public static void loadFile(String path,Connection sqlCon,int unibase){
        String [] children = getFile(path,file);
        if (children == null) {
            System.out.println("Either dir does not exist or is not a directory");
        } else {
            for (String filename : children) {
                disableTrigger(sqlCon,tableName);
                readOnFile(path, filename, tableName + "_DEST", sqlCon);
                executeQuery(sqlCon, renameExistingPieceLink(tableName + "_DEST"));
                updateTableDest(sqlCon, "", "'RG_No','DR_No'", tableName, tableName + "_DEST",filename,unibase);
                sendData(sqlCon, path, filename, insert(filename));
            //    deleteTempTable(sqlCon, tableName+"_DEST");
                enableTrigger(sqlCon,tableName);
            }
        }
    }

    public static void loadDeleteFile(String path,Connection sqlCon) {
        String [] children = getFile(path,"deleteList"+file);
        disableTrigger(sqlCon,tableName);
        if (children == null) {
            System.out.println("Either dir does not exist or is not a directory");
        } else {
            for (String filename : children) {
                String deleteReglEch = deleteReglEch();
                loadDeleteInfo(path,tableName,filename,sqlCon,deleteReglEch);
            }
        }
        enableTrigger(sqlCon,tableName);
    }

    public static void getDataElement(Connection sqlCon, String path, String database, String time, JSONObject type)
    {
        String filename =  file+time+".csv";
        dbSource = database;
        initTableParam(sqlCon,tableName,configList,"DO_Domaine,DO_Type,DO_Piece,DR_No,RG_No,DataBaseSource");
        getData(sqlCon, selectSourceTable(tableName,database,type), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);

    }
    public static void getDataElementFilterAgency(Connection sqlCon, String path,String database,String time,String agency)
    {
        String filename =  file+time+".csv";
        dbSource = database;
        initTableParam(sqlCon,tableName,configList,"DO_Domaine,DO_Type,DO_Piece,DR_No,RG_No,DataBaseSource");
        getData(sqlCon, selectSourceTableFilterAgencyEnteteLink(tableName,database,agency), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);

    }

    public static void deleteTempTable(Connection sqlCon)
    {
        String query = """
                IF OBJECT_ID('F_REGLECH_DEST') IS NOT NULL
                    DROP TABLE F_REGLECH_DEST;\
                """;
        executeQuery(sqlCon, query);
    }

    public static String deleteReglEch()
    {
        return
                """
                         DELETE FROM F_REGLECH
                         WHERE EXISTS (SELECT 1 FROM F_REGLECH_SUPPR WHERE ISNULL(F_REGLECH.DataBaseSource,'') = ISNULL(F_REGLECH_SUPPR.DataBaseSource,'') AND ISNULL(F_REGLECH.cbMarqSource,0) = ISNULL(F_REGLECH_SUPPR.cbMarq,0) )

                         IF OBJECT_ID('F_REGLECH_SUPPR') IS NOT NULL
                         DROP TABLE F_REGLECH_SUPPR
                        """;
    }
}
