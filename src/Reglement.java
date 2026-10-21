import java.sql.Connection;

public class Reglement extends Table {

    public static String file = "Reglement_";
    public static String dbSource = "BIJOU";
    public static String tableName = "F_CREGLEMENT";
    public static String configList = "listReglement";
    public static String list()
    {
        return """
                SELECT    [RG_No],[CT_NumPayeur],[RG_Date],[RG_Reference],[RG_Libelle],[RG_Montant],[RG_MontantDev],[N_Reglement],[RG_Impute]
                        ,[RG_Compta],[EC_No],[RG_Type],[RG_Cours],[N_Devise],[JO_Num],[CG_NumCont],[RG_Impaye]
                        ,[CG_Num],[RG_TypeReg],[RG_Heure],[RG_Piece],[CA_No],[CO_NoCaissier],[RG_Banque],[RG_Transfere]
                        ,[RG_Cloture],[RG_Ticket],[RG_Souche],[CT_NumPayeurOrig],[RG_DateEchCont],[CG_NumEcart],[JO_NumEcart],[RG_MontantEcart]
                        ,[RG_NoBonAchat],[cbProt],[cbMarq],[cbCreateur],[cbModification],[cbReplication],[cbFlag], cbMarqSource = cbMarq
                        ,[DataBaseSource] = '%s'
                FROM    [F_CREGLEMENT]
                WHERE    cbModification>= ISNULL((SELECT LastSynchro FROM config.SelectTable WHERE tableName = 'F_CREGLEMENT'),'1900-01-01')\
                """.formatted(dbSource);
    }

    public static String insert(String filename)
    {
        return """
                BEGIN TRY  SET DATEFORMAT ymd;
                IF OBJECT_ID('tempdb..#reglement') IS NOT NULL
                    DROP TABLE #reglement
                 IF OBJECT_ID('F_CREGLEMENT_DEST') IS NOT NULL
                UPDATE    F_CREGLEMENT_DEST
                SET    [RG_Montant] = REPLACE([RG_Montant],',','.')
                    ,[RG_MontantDev] = REPLACE([RG_MontantDev],',','.')
                    ,[RG_MontantEcart] = REPLACE([RG_MontantEcart],',','.')
                    , [RG_Cours] = REPLACE([RG_Cours],',','.')

                SELECT RG_No = ISNULL((SELECT Max(RG_No) FROM F_CREGLEMENT),0) + ROW_NUMBER() OVER(ORDER BY dest.RG_No)
                ,dest.[CT_NumPayeur],dest.[RG_Date],dest.[RG_Reference],dest.[RG_Libelle],dest.[RG_Montant],dest.[RG_MontantDev],dest.[N_Reglement],dest.[RG_Impute]
                ,dest.[RG_Compta],dest.[EC_No],dest.[RG_Type],dest.[RG_Cours],dest.[N_Devise],dest.[JO_Num],dest.[CG_NumCont],dest.[RG_Impaye]
                ,dest.[CG_Num],dest.[RG_TypeReg],dest.[RG_Heure],dest.[RG_Piece],CA_No = ISNULL(cai.[CA_No],dest.[CA_No]),dest.[CO_NoCaissier],dest.[RG_Banque],dest.[RG_Transfere]
                ,dest.[RG_Cloture],dest.[RG_Ticket],dest.[RG_Souche],dest.[CT_NumPayeurOrig],dest.[RG_DateEchCont],dest.[CG_NumEcart],dest.[JO_NumEcart],dest.[RG_MontantEcart]
                ,dest.[RG_NoBonAchat],dest.[cbProt],dest.[cbCreateur],dest.[cbModification],dest.[cbReplication],dest.[cbFlag],dest.[cbMarqSource],dest.[dataBaseSource],RG_NoSource = dest.RG_No
                ,RG_NoSrc = src.RG_No into #reglement
                FROM F_CREGLEMENT_DEST dest
                LEFT JOIN (SELECT RG_No,RG_NoSource,dataBaseSource FROM F_CREGLEMENT) src
                ON ISNULL(dest.RG_No,0) = ISNULL(src.RG_NoSource,0)
                AND ISNULL(dest.dataBaseSource,'') = ISNULL(src.dataBaseSource,'')
                LEFT JOIN F_CAISSE cai ON ISNULL(cai.CA_NoSource,0) = ISNULL(dest.CA_No,0) AND ISNULL(cai.dataBaseSource,'') = ISNULL(dest.dataBaseSource,'')
                {{FK_CHECK}}
                 IF OBJECT_ID('F_CREGLEMENT_DEST') IS NOT NULL AND {{UPDATE_ENABLED}} = 1
                UPDATE cre
                   SET [CT_NumPayeur] = cre.CT_NumPayeur
                      ,[RG_Date] = cre.RG_Date
                      ,[RG_Reference] = cre.RG_Reference
                      ,[RG_Libelle] = cre.RG_Libelle
                      ,[RG_Montant] = cre.RG_Montant
                      ,[RG_MontantDev] = cre.RG_MontantDev
                      ,[N_Reglement] = cre.N_Reglement
                      ,[RG_Impute] = cre.RG_Impute
                      ,[RG_Compta] = cre.RG_Compta
                      ,[EC_No] = cre.EC_No
                      ,[RG_Type] = cre.RG_Type
                      ,[RG_Cours] = cre.RG_Cours
                      ,[N_Devise] = cre.N_Devise
                      ,[JO_Num] = cre.JO_Num
                      ,[CG_NumCont] = cre.CG_NumCont
                      ,[RG_Impaye] = cre.RG_Impaye
                      ,[CG_Num] = cre.CG_Num
                      ,[RG_TypeReg] = cre.RG_TypeReg
                      ,[RG_Heure] = cre.RG_Heure
                      ,[RG_Piece] = cre.RG_Piece
                      ,[CA_No] = cre.CA_No
                      ,[CO_NoCaissier] = cre.CO_NoCaissier
                      ,[RG_Banque] = cre.RG_Banque
                      ,[RG_Transfere] = cre.RG_Transfere
                      ,[RG_Cloture] = cre.RG_Cloture
                      ,[RG_Ticket] = cre.RG_Ticket
                      ,[RG_Souche] = cre.RG_Souche
                      ,[CT_NumPayeurOrig] = cre.CT_NumPayeurOrig
                      ,[RG_DateEchCont] = cre.RG_DateEchCont
                      ,[CG_NumEcart] = cre.CG_NumEcart
                      ,[JO_NumEcart] = cre.JO_NumEcart
                      ,[RG_MontantEcart] = cre.RG_MontantEcart
                      ,[RG_NoBonAchat] = cre.RG_NoBonAchat
                      ,[cbProt] = cre.cbProt
                      ,[cbCreateur] = cre.cbCreateur
                      ,[cbModification] = cre.cbModification
                      ,[cbReplication] = cre.cbReplication
                      ,[cbFlag] = cre.cbFlag
                      FROM F_CREGLEMENT cre
                      INNER JOIN #reglement tmp ON ISNULL(cre.[DataBaseSource],'') = ISNULL(tmp.[DataBaseSource],'')
                      AND ISNULL(cre.RG_NoSource,0) = ISNULL(tmp.RG_NoSource,0)
                 IF OBJECT_ID('F_CREGLEMENT_DEST') IS NOT NULL
                INSERT INTO F_CREGLEMENT (
                [RG_No],[CT_NumPayeur],[RG_Date],[RG_Reference],[RG_Libelle],[RG_Montant],[RG_MontantDev],[N_Reglement],[RG_Impute]
                        ,[RG_Compta],[EC_No],[RG_Type],[RG_Cours],[N_Devise],[JO_Num],[CG_NumCont],[RG_Impaye]
                        ,[CG_Num],[RG_TypeReg],[RG_Heure],[RG_Piece],[CA_No],[CO_NoCaissier],[RG_Banque],[RG_Transfere]
                        ,[RG_Cloture],[RG_Ticket],[RG_Souche],[CT_NumPayeurOrig],[RG_DateEchCont],[CG_NumEcart],[JO_NumEcart],[RG_MontantEcart]
                        ,[RG_NoBonAchat],[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag],cbMarqSource,DataBaseSource,RG_NoSource)

                SELECT    RG_No
                        ,[CT_NumPayeur],[RG_Date],[RG_Reference],[RG_Libelle],[RG_Montant],[RG_MontantDev],[N_Reglement],[RG_Impute]
                        ,[RG_Compta],[EC_No],[RG_Type],[RG_Cours],[N_Devise],[JO_Num],[CG_NumCont],[RG_Impaye]
                        ,[CG_Num],[RG_TypeReg],[RG_Heure],[RG_Piece],[CA_No],[CO_NoCaissier],[RG_Banque],[RG_Transfere]
                        ,[RG_Cloture],[RG_Ticket],[RG_Souche],[CT_NumPayeurOrig],[RG_DateEchCont],[CG_NumEcart],[JO_NumEcart],[RG_MontantEcart]
                        ,[RG_NoBonAchat],[cbProt],[cbCreateur],[cbModification],[cbReplication],[cbFlag],[cbMarqSource],[dataBaseSource],[RG_NoSource]
                FROM #reglement
                WHERE RG_NoSrc IS NULL

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
                   'F_REGLEMENT',
                   GETDATE());
                END CATCH\
                """.formatted(filename)
                .replace("{{FK_CHECK}}", foreignKeyCheck(tableName, "#reglement",
                        "N'RG_No source : ' + CAST(t.RG_NoSource AS NVARCHAR(20)) + N' Piece : ' + ISNULL(t.RG_Piece, N'')"
                                + " + N' cbMarq : ' + CAST(t.cbMarqSource AS NVARCHAR(20)) + N' database : ' + ISNULL(t.DataBaseSource, N'')",
                        filename));
    }

    public static void sendDataElement(Connection sqlCon, String path,String database,int unibase)
    {
        dbSource = database;
        loadFile(path,sqlCon);
        loadDeleteFile(path,sqlCon);
//        DocEntete.loadDeleteFile(path,sqlCon);
    }

    public static void loadDeleteFile(String path,Connection sqlCon) {
        String [] children = getFile(path,"deleteList"+file);
        disableTrigger(sqlCon,tableName);
        if (children == null) {
            System.out.println("Either dir does not exist or is not a directory");
        } else {
            for (String filename : children){
                String deleteReglement = deleteReglement();
                loadDeleteInfo(path,tableName,filename,sqlCon,deleteReglement);
            }
        }
        enableTrigger(sqlCon,tableName);
    }

    public static void loadFile(String path,Connection sqlCon){
        String [] children = getFile(path,file);
        if (children == null) {
            System.out.println("Either dir does not exist or is not a directory");
        } else {
            for (String filename : children){
                disableTrigger(sqlCon,tableName);
                readOnFile(path, filename, tableName + "_DEST", sqlCon);
                sendData(sqlCon, path, filename, applyUpdateFlag(insert(filename), tableName));
                //deleteTempTable(sqlCon, tableName+"_DEST");
                enableTrigger(sqlCon,tableName);
            }
        }
    }

    public static void getDataElement(Connection sqlCon, String path,String database,String time)
    {
        String filename =  file+time+".csv";
        dbSource = database;
        initTableParam(sqlCon,tableName,configList,"RG_No,DatabaseSource");
        getData(sqlCon, selectSourceTable(tableName,database,true), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);
    }

    public static void getDataElementFilterAgency(Connection sqlCon, String path,String database,String time,String agency)
    {
        String filename =  file+time+".csv";
        dbSource = database;
        initTableParam(sqlCon,tableName,configList,"RG_No,DatabaseSource");
        getData(sqlCon, selectSourceTableFilterAgencyRegltLink(tableName,database,agency), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);
    }

    public static String deleteReglement()
    {
        return
                """
                         DELETE FROM F_CREGLEMENT
                         WHERE EXISTS (SELECT 1 FROM F_CREGLEMENT_SUPPR WHERE ISNULL(F_CREGLEMENT.RG_NoSource,0) = ISNULL(F_CREGLEMENT_SUPPR.RG_No,0) AND ISNULL(F_CREGLEMENT.DataBaseSource,'') = ISNULL(F_CREGLEMENT_SUPPR.DataBaseSource,''))

                         IF OBJECT_ID('F_CREGLEMENT_SUPPR') IS NOT NULL
                         DROP TABLE F_CREGLEMENT_SUPPR
                        """;
    }

}
