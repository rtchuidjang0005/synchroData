import java.sql.Connection;

public class Caisse extends Table {
    public static String file ="caisse_";
    public static String dbSource = "BIJOU";
    public static String tableName = "F_CAISSE";
    public static String configList = "listCaisse";

    public static String insert(String filename)
    {
        return
                """
                         BEGIN TRY
                        SET DATEFORMAT ymd;
                        IF OBJECT_ID('tempdb..#TmpCaisse') IS NOT NULL
                            DROP TABLE #TmpCaisse

                        SELECT CA_No = ISNULL((SELECT MAX(CA_No) FROM F_CAISSE),0) + ROW_NUMBER() OVER(ORDER BY dest.CA_No)
                        ,[CA_Intitule],DE_No = ISNULL(dep.DE_No,dest.[DE_No]),[CO_No]
                                   ,[CO_NoCaissier],[CT_Num],[JO_Num],[CA_IdentifCaissier]
                                   ,[CA_DateCreation],[N_Comptoir],[N_Clavier]
                           ,[CA_LignesAfficheur],[CA_ColonnesAfficheur],[CA_ImpTicket]
                                   ,[CA_SaisieVendeur],[CA_Souche],[cbProt],[cbCreateur]
                                   ,[cbModification],[cbReplication],[cbFlag]
                           ,CA_NoSource = dest.CA_No, dest.cbMarqSource,dest.DatabaseSource,[CA_NoSourceCai] = src.CA_NoSource
                           INTO #TmpCaisse
                        FROM F_CAISSE_DEST dest
                        LEFT JOIN (SELECT CA_NoSource,DatabaseSource FROM F_CAISSE) src
                        ON ISNULL(src.CA_NoSource,0) = ISNULL(dest.CA_No,0)
                        AND ISNULL(src.DatabaseSource,'') = ISNULL(dest.DatabaseSource,'')
                        LEFT JOIN (SELECT DE_NoSource,DatabaseSource,DE_No FROM F_DEPOT) dep
                        ON ISNULL(dep.DE_NoSource,0) = ISNULL(dest.DE_No,0)
                        AND ISNULL(dep.DataBaseSource,'') = ISNULL(dest.DataBaseSource,'')
                        IF {{UPDATE_ENABLED}} = 1
                        UPDATE dwh SET
                              [CA_Intitule] = tmp.CA_Intitule
                              ,[DE_No] = tmp.DE_No
                              ,[CO_No] = tmp.CO_No
                              ,[CO_NoCaissier] = tmp.CO_NoCaissier
                              ,[CT_Num] = tmp.CT_Num
                              ,[JO_Num] = tmp.JO_Num
                              ,[CA_IdentifCaissier] = tmp.CA_IdentifCaissier
                              ,[CA_DateCreation] = tmp.CA_DateCreation
                              ,[N_Comptoir] = tmp.N_Comptoir
                              ,[N_Clavier] = tmp.N_Clavier
                              ,[CA_LignesAfficheur] = tmp.CA_LignesAfficheur
                              ,[CA_ColonnesAfficheur] = tmp.CA_ColonnesAfficheur
                              ,[CA_ImpTicket] = tmp.CA_ImpTicket
                              ,[CA_SaisieVendeur] = tmp.CA_SaisieVendeur
                              ,[CA_Souche] = tmp.CA_Souche
                              ,[cbProt] = tmp.cbProt
                              ,[cbCreateur] = tmp.cbCreateur
                              ,[cbModification] = tmp.cbModification
                              ,[cbReplication] = tmp.cbReplication
                              ,[cbFlag] = tmp.cbFlag
                        FROM F_CAISSE dwh
                        INNER JOIN #TmpCaisse tmp ON dwh.CA_NoSource = tmp.CA_NoSource
                        AND ISNULL(dwh.DatabaseSource,'') = ISNULL(tmp.DatabaseSource,'')

                        INSERT INTO [dbo].[F_CAISSE]
                                   ([CA_No],[CA_Intitule],[DE_No],[CO_No]
                                   ,[CO_NoCaissier],[CT_Num],[JO_Num],[CA_IdentifCaissier]
                                   ,[CA_DateCreation],[N_Comptoir],[N_Clavier]
                                   ,[CA_LignesAfficheur],[CA_ColonnesAfficheur],[CA_ImpTicket]
                                   ,[CA_SaisieVendeur],[CA_Souche],[cbProt],[cbCreateur]
                                   ,[cbModification],[cbReplication],[cbFlag]
                                   ,CA_NoSource,cbMarqSource,DatabaseSource)

                        SELECT [CA_No]
                        , [CA_Intitule],[DE_No],[CO_No]
                                   ,[CO_NoCaissier],[CT_Num],[JO_Num],[CA_IdentifCaissier]
                                   ,[CA_DateCreation],[N_Comptoir],[N_Clavier]
                           ,[CA_LignesAfficheur],[CA_ColonnesAfficheur],[CA_ImpTicket]
                                   ,[CA_SaisieVendeur],[CA_Souche],[cbProt],[cbCreateur]
                                   ,[cbModification],[cbReplication],[cbFlag]
                           ,CA_NoSource, cbMarqSource,DatabaseSource
                        FROM #TmpCaisse
                        WHERE [CA_NoSourceCai]  IS NULL
                        END TRY BEGIN CATCH
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
                           'F_CAISSE',
                           GETDATE());
                        END CATCH

                        """.formatted(filename);
    }

    public static void sendDataElement(Connection sqlCon, String path,String database,int unibase)
    {
        dbSource = database;
        loadFile(path,sqlCon);
        loadDeleteFile(path,sqlCon);
        DocEntete.loadDeleteFile(path,sqlCon);
    }

    public static void loadDeleteFile(String path,Connection sqlCon) {
        String [] children = getFile(path,"deleteList"+file);
        if (children == null) {
            System.out.println("Either dir does not exist or is not a directory");
        } else {
            for (String filename : children){
                String deleteCaisse = deleteCaisse();
                loadDeleteInfo(path,tableName,filename,sqlCon,deleteCaisse);
            }
        }
    }

    public static void loadFile(String path,Connection sqlCon){
        String [] children = getFile(path,file);
        if (children == null) {
            System.out.println("Either dir does not exist or is not a directory");
        } else {
            for (String filename : children){
                readOnFile(path, filename, tableName + "_DEST", sqlCon);
                sendData(sqlCon, path, filename, applyUpdateFlag(insert(filename), tableName));
                //deleteTempTable(sqlCon, tableName+"_DEST");
            }
        }
    }

    public static void getDataElement(Connection sqlCon, String path,String database,String time)
    {
        String filename =  file+time+".csv";
        initTableParam(sqlCon,tableName,configList,"CA_No,DatabaseSource");
        getData(sqlCon, selectSourceTable(tableName,database,true), tableName, path, filename);
        listDeleteAllInfo(sqlCon, path, "deleteList" + filename,tableName,configList,database);
    }

    public static String deleteCaisse()
    {
        return
                """
                         DELETE src
                         FROM F_CAISSE src
                         INNER JOIN F_CAISSE_SUPPR del
                         ON ISNULL(src.CA_No,0) = ISNULL(del.CA_No,0)
                         AND ISNULL(src.DatabaseSource,'') = ISNULL(del.DatabaseSource,'') ;
                         IF OBJECT_ID('F_CAISSE_SUPPR') IS NOT NULL
                         DROP TABLE F_CAISSE_SUPPR ;\
                        """;
    }

}
