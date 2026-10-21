import org.json.simple.JSONObject;

import java.sql.Connection;

public class DocRegl extends Table {

    public static String file = "docRegl_";
    public static String dbSource = "BIJOU";
    public static String tableName = "F_DOCREGL";
    public static String configList = "listDocRegl";

    public static String insert(String filename)
    {
        return  """
                BEGIN TRY  SET DATEFORMAT ymd;
                 IF OBJECT_ID('F_DOCREGL_DEST') IS NOT NULL
                 UPDATE    F_DOCREGL_DEST
                SET    [DR_Pourcent] = REPLACE([DR_Pourcent],',','.')
                    ,[DR_Montant] = REPLACE([DR_Montant],',','.')
                    , [DR_MontantDev] = REPLACE([DR_MontantDev],',','.')

                 IF OBJECT_ID('F_DOCREGL_DEST') IS NOT NULL AND {{UPDATE_ENABLED}} = 1
                UPDATE src
                   SET [DR_TypeRegl] = dest.DR_TypeRegl
                      ,[DR_Date] = dest.DR_Date
                      ,[DR_Libelle] = dest.DR_Libelle
                      ,[DR_Pourcent] = dest.DR_Pourcent
                      ,[DR_Montant] = dest.DR_Montant
                      ,[DR_MontantDev] = dest.DR_MontantDev
                      ,[DR_Equil] = dest.DR_Equil
                      ,[EC_No] = dest.EC_No
                      ,[DR_Regle] = dest.DR_Regle
                      ,[N_Reglement] = dest.N_Reglement
                      ,[cbProt] = dest.cbProt
                      ,[cbCreateur] = dest.cbCreateur
                      ,[cbModification] = dest.cbModification
                      ,[cbReplication] = dest.cbReplication
                      ,[cbFlag] = dest.cbFlag
                      ,[DR_NoSource] = dest.DR_No
                FROM [dbo].[F_DOCREGL] src
                INNER JOIN F_DOCREGL_DEST dest ON src.cbMarqSource = dest.cbMarqSource
                    AND    ISNULL(src.DataBaseSource,'') = ISNULL(dest.DataBaseSource,'')

                 IF OBJECT_ID('F_DOCREGL_DEST') IS NOT NULL
                BEGIN
                IF OBJECT_ID('tempdb..#DocRegl') IS NOT NULL
                    DROP TABLE #DocRegl

                SELECT     [DR_No] = ISNULL((SELECT Max(DR_No) FROM F_DOCREGL),0) + ROW_NUMBER() OVER(ORDER BY dest.DR_No)
                        ,dest.[DO_Domaine],dest.[DO_Type],dest.[DO_Piece],[DR_TypeRegl],[DR_Date]
                        ,[DR_Libelle],[DR_Pourcent],[DR_Montant],[DR_MontantDev],[DR_Equil],[EC_No],[DR_Regle]
                        ,[N_Reglement],[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag],dest.cbMarqSource,[DataBaseSource] = dest.[dataBaseSource],[DR_NoSource] = dest.DR_No
                INTO #DocRegl
                FROM F_DOCREGL_DEST dest
                LEFT JOIN (SELECT cbMarqSource,DataBaseSource FROM F_DOCREGL) src
                    ON    ISNULL(dest.cbMarqSource,0) = ISNULL(src.cbMarqSource,0)
                    AND    ISNULL(dest.DataBaseSource,'') = ISNULL(src.DataBaseSource,'')
                LEFT JOIN (SELECT DO_Piece,DO_Type,DO_Domaine FROM F_DOCENTETE) docE
                ON docE.DO_Domaine = dest.DO_Domaine
                AND docE.DO_Type = dest.DO_Type
                AND docE.DO_Piece = dest.DO_Piece
                WHERE src.cbMarqSource IS NULL
                AND docE.DO_Piece IS NOT NULL

                {{FK_CHECK}}

                INSERT INTO F_DOCREGL (
                [DR_No],[DO_Domaine],[DO_Type],[DO_Piece],[DR_TypeRegl],[DR_Date]
                        ,[DR_Libelle],[DR_Pourcent],[DR_Montant],[DR_MontantDev],[DR_Equil],[EC_No],[DR_Regle]
                        ,[N_Reglement],[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag],cbMarqSource,DataBaseSource,DR_NoSource)
                SELECT [DR_No],[DO_Domaine],[DO_Type],[DO_Piece],[DR_TypeRegl],[DR_Date]
                        ,[DR_Libelle],[DR_Pourcent],[DR_Montant],[DR_MontantDev],[DR_Equil],[EC_No],[DR_Regle]
                        ,[N_Reglement],[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag],cbMarqSource,DataBaseSource,DR_NoSource
                FROM #DocRegl
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
                SELECT       SUSER_SNAME(),           NULL,           NULL,           NULL,           NULL,           NULL,           NULL,'Domaine : '+CAST(dest.[DO_Domaine] AS VARCHAR(150))+' Type : ' + CAST(dest.[DO_Type]  AS VARCHAR(150)) + ' Piece : ' + CAST(dest.[DO_Piece] AS VARCHAR(150))           +' cbMarq : ' + CAST(dest.[cbMarqSource] AS VARCHAR(150)) + ' database : ' + CAST(dest.[DataBaseSource] AS VARCHAR(150))           + 'fileName : %s '           ,'F_DOCREGL'
                           ,GETDATE()
                FROM F_DOCREGL_DEST dest
                LEFT JOIN (SELECT cbMarqSource,DataBaseSource FROM F_DOCREGL) src
                    ON    ISNULL(dest.cbMarqSource,0) = ISNULL(src.cbMarqSource,0)
                    AND    ISNULL(dest.DataBaseSource,'') = ISNULL(src.DataBaseSource,'')
                LEFT JOIN (SELECT DO_Piece,DO_Type,DO_Domaine FROM F_DOCENTETE) docE
                ON docE.DO_Domaine = dest.DO_Domaine
                AND docE.DO_Type = dest.DO_Type
                AND docE.DO_Piece = dest.DO_Piece
                WHERE src.cbMarqSource IS NULL
                AND docE.DO_Piece IS NULL
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
                   'F_DOCREGL',
                   GETDATE());
                END CATCH\
                """.formatted(filename, filename)
                .replace("{{FK_CHECK}}", foreignKeyCheck(tableName, "#DocRegl",
                        "N'Domaine : ' + CAST(t.DO_Domaine AS NVARCHAR(10)) + N' Type : ' + CAST(t.DO_Type AS NVARCHAR(10))"
                                + " + N' Piece : ' + t.DO_Piece + N' DR_No source : ' + CAST(t.DR_NoSource AS NVARCHAR(20))"
                                + " + N' cbMarq : ' + CAST(t.cbMarqSource AS NVARCHAR(20)) + N' database : ' + ISNULL(t.DataBaseSource, N'')",
                        filename));
    }
    public static void sendDataElement(Connection sqlCon, String path,String database,int unibase)
    {
        dbSource = database;
        loadFile( path, sqlCon,unibase);
        loadDeleteFile(path,sqlCon);
//        DocEntete.loadDeleteFile(path,sqlCon);
    }

    public static void loadFile(String path,Connection sqlCon,int unibase){
        String [] children = getFile(path,file);
        if (children == null) {
            System.out.println("Either dir does not exist or is not a directory");
        } else {
            for (String filename : children){
                disableTrigger(sqlCon,tableName);
                readOnFile(path, filename, tableName + "_DEST", sqlCon);
                executeQuery(sqlCon, renameExistingPieceLink(tableName + "_DEST"));
                updateTableDest(sqlCon, "", "'DR_No'", tableName, tableName + "_DEST",filename,unibase);
                sendData(sqlCon, path, filename, applyUpdateFlag(insert(filename), tableName));
               // deleteTempTable(sqlCon, tableName+"_DEST");
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
            for (String filename : children){
                String deleteDocEntete = deleteDocRegl();
                loadDeleteInfo(path,tableName,filename,sqlCon,deleteDocEntete);
            }
        }
        enableTrigger(sqlCon,tableName);
    }

    public static void getDataElement(Connection sqlCon, String path, String database, String time, JSONObject type)
    {
        String filename =  file+time+".csv";
        dbSource = database;
        initTableParam(sqlCon,tableName,configList,"DO_Domaine,DO_Type,DO_Piece,DR_No,DatabaseSource");
        getData(sqlCon, selectSourceTable(tableName,database,type), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);

    }
    public static void getDataElementFilterAgency(Connection sqlCon, String path,String database,String time,String agency)
    {
        String filename =  file+time+".csv";
        dbSource = database;
        initTableParam(sqlCon,tableName,configList,"DO_Domaine,DO_Type,DO_Piece,DR_No,DatabaseSource");
        getData(sqlCon, selectSourceTableFilterAgencyEnteteLink(tableName,database,agency), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);

    }

    public static String deleteDocRegl()
    {
        return
                """
                         DELETE FROM F_DOCREGL
                         WHERE EXISTS (SELECT 1 FROM F_DOCREGL_SUPPR WHERE ISNULL(F_DOCREGL.DataBaseSource,'') = ISNULL(F_DOCREGL_SUPPR.DataBaseSource,'') AND ISNULL(F_DOCREGL.DR_NoSource,0) = ISNULL(F_DOCREGL_SUPPR.DR_No,0) )

                         IF OBJECT_ID('F_DOCENTETE_SUPPR') IS NOT NULL
                         DROP TABLE F_DOCENTETE_SUPPR
                        """;
    }

}
