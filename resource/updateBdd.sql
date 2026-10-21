GO
IF OBJECT_ID('[dbo].[Z_LiaisonEnvoiSMSUser]', 'U') IS NULL
CREATE TABLE [dbo].[Z_LiaisonEnvoiSMSUser](
                                              [TE_No] [int] NULL,
                                              [Prot_No] [int] NULL,
                                              [cbModification] [datetime] NULL,
                                              [cbUser] [int] NULL
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[TmpLib]    Script Date: 28/08/2018 20:12:46 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF OBJECT_ID('[dbo].[TmpLib]', 'U') IS NULL
CREATE TABLE [dbo].[TmpLib](
    [lib] [varchar](500) NULL
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Z_CODECLIENT]    Script Date: 28/08/2018 20:12:47 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF OBJECT_ID('[dbo].[Z_CODECLIENT]', 'U') IS NULL
CREATE TABLE [dbo].[Z_CODECLIENT](
                                     [CodeClient] [varchar](13) NOT NULL,
                                     [Libelle_ville] [varchar](50) NULL,
                                     [CT_Type] [int] NULL
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Z_DEPOTCAISSE]    Script Date: 28/08/2018 20:12:47 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF OBJECT_ID('[dbo].[Z_DEPOTCAISSE]', 'U') IS NULL
CREATE TABLE [dbo].[Z_DEPOTCAISSE](
                                      [DE_No] [int] NOT NULL,
                                      [CA_No] [int] NOT NULL
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Z_DEPOTCLIENT]    Script Date: 28/08/2018 20:12:47 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF OBJECT_ID('[dbo].[Z_DEPOTCLIENT]', 'U') IS NULL
CREATE TABLE [dbo].[Z_DEPOTCLIENT](
                                      [DE_No] [int] NOT NULL,
                                      [CodeClient] [varchar](13) NOT NULL
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Z_DEPOTSOUCHE]    Script Date: 28/08/2018 20:12:47 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF OBJECT_ID('[dbo].[Z_DEPOTSOUCHE]', 'U') IS NULL
CREATE TABLE [dbo].[Z_DEPOTSOUCHE](
                                      [DE_No] [int] NOT NULL,
                                      [CA_SoucheVente] [int] NULL,
                                      [CA_SoucheAchat] [int] NULL,
                                      [CA_SoucheStock] [int] NULL,
                                      [CA_Num] [varchar](13) NULL
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Z_DEPOTUSER]    Script Date: 28/08/2018 20:12:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF OBJECT_ID('[dbo].[Z_DEPOTUSER]', 'U') IS NULL
CREATE TABLE [dbo].[Z_DEPOTUSER](
                                    [Prot_No] [int] NOT NULL,
                                    [DE_No] [int] NOT NULL,
                                    [IsPrincipal] [int] NULL
) ON [PRIMARY]
GO
IF OBJECT_ID('[dbo].[Z_DEPOTEMPLUSER]', 'U') IS NULL
CREATE TABLE [dbo].[Z_DEPOTEMPLUSER](
                                        [Prot_No] [int] NOT NULL,
                                        [DP_No] [int] NOT NULL
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Z_ECRITURECPIECE]    Script Date: 28/08/2018 20:12:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF OBJECT_ID('[dbo].[Z_ECRITURECPIECE]', 'U') IS NULL
CREATE TABLE [dbo].[Z_ECRITURECPIECE](
                                         [EC_No] [int] NOT NULL,
                                         [Lien_Fichier] [varchar](max) NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Z_FACT_REGL_SUPPR]    Script Date: 28/08/2018 20:12:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF OBJECT_ID('[dbo].[Z_FACT_REGL_SUPPR]', 'U') IS NULL
CREATE TABLE [dbo].[Z_FACT_REGL_SUPPR](
                                          [DO_Domaine] [int] NOT NULL,
                                          [DO_Type] [int] NOT NULL,
                                          [DO_Piece] [varchar](25) NOT NULL,
                                          [CbMarq_Entete] [int] NOT NULL,
                                          [RG_No] [int] NOT NULL,
                                          [CbMarq_RG] [int] NOT NULL
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Z_LiaisonEnvoiMailUser]    Script Date: 28/08/2018 20:12:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF OBJECT_ID('[dbo].[Z_LiaisonEnvoiMailUser]', 'U') IS NULL
CREATE TABLE [dbo].[Z_LiaisonEnvoiMailUser](
                                               [TE_No] [int] NULL,
                                               [Prot_No] [int] NULL,
                                               [cbModification] [datetime] NULL,
                                               [cbUser] [int] NULL
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Z_LIGNE_COMPTEA]    Script Date: 28/08/2018 20:12:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF OBJECT_ID('[dbo].[Z_LIGNE_COMPTEA]', 'U') IS NULL
CREATE TABLE [dbo].[Z_LIGNE_COMPTEA](
                                        [CbMarq_Ligne] [int] NULL,
                                        [N_Analytique] [smallint] NOT NULL,
                                        [CA_Num] [varchar](13) NOT NULL,
                                        [EA_Ligne] [int] NULL,
                                        [EA_Montant] [float] NULL,
                                        [EA_Quantite] [float] NULL,
                                        [cbMarq] [int] IDENTITY(1,1) NOT NULL,
                                        [cbModification] [smalldatetime] NULL
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Z_REGLEMENTPIECE]    Script Date: 28/08/2018 20:12:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF OBJECT_ID('[dbo].[Z_REGLEMENTPIECE]', 'U') IS NULL
CREATE TABLE [dbo].[Z_REGLEMENTPIECE](
                                         [RG_No] [int] NOT NULL,
                                         [Lien_Fichier] [varchar](max) NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Z_RGLT_BONDECAISSE]    Script Date: 28/08/2018 20:12:48 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF OBJECT_ID('[dbo].[Z_RGLT_BONDECAISSE]', 'U') IS NULL
CREATE TABLE [dbo].[Z_RGLT_BONDECAISSE](
                                           [RG_No] [int] NOT NULL,
                                           [RG_No_RGLT] [int] NOT NULL
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Z_SMS]    Script Date: 28/08/2018 20:12:49 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF OBJECT_ID('[dbo].[Z_SMS]', 'U') IS NULL
CREATE TABLE [dbo].[Z_SMS](
                              [ID] [int] NOT NULL,
                              [CA_No] [int] NULL,
                              [MSG] [varchar](250) NULL,
                              [NUMBER_R] [varchar](50) NULL,
                              [DATE_S] [smalldatetime] NULL,
                              [ETAT] [varchar](2) NULL
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Z_TypeEnvoiMail]    Script Date: 28/08/2018 20:12:49 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
IF OBJECT_ID('[dbo].[Z_TypeEnvoiMail]', 'U') IS NULL
CREATE TABLE [dbo].[Z_TypeEnvoiMail](
                                        [TE_No] [int] IDENTITY(1,1) NOT NULL,
                                        [TE_Intitule] [varchar](150) NULL,
                                        PRIMARY KEY CLUSTERED
                                            (
                                             [TE_No] ASC
                                                )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

IF NOT EXISTS (SELECT 1 FROM  Z_TypeEnvoiMail WHERE TE_Intitule = 'Suppression règlement')
    INSERT INTO Z_TypeEnvoiMail values('Suppression règlement');
IF NOT EXISTS (SELECT 1 FROM  Z_TypeEnvoiMail WHERE TE_Intitule = 'Versement distant')
    INSERT INTO Z_TypeEnvoiMail values('Versement distant');
IF NOT EXISTS (SELECT 1 FROM  Z_TypeEnvoiMail WHERE TE_Intitule = 'Mouvement de sortie')
    INSERT INTO Z_TypeEnvoiMail values('Mouvement de sortie');
IF NOT EXISTS (SELECT 1 FROM  Z_TypeEnvoiMail WHERE TE_Intitule = 'Versement bancaire')
    INSERT INTO Z_TypeEnvoiMail values('Versement bancaire');
IF NOT EXISTS (SELECT 1 FROM  Z_TypeEnvoiMail WHERE TE_Intitule = 'Modification de la facture')
    INSERT INTO Z_TypeEnvoiMail values('Modification de la facture');
IF NOT EXISTS (SELECT 1 FROM  Z_TypeEnvoiMail WHERE TE_Intitule = 'Prix modifié')
    INSERT INTO Z_TypeEnvoiMail values('Prix modifié');
IF NOT EXISTS (SELECT 1 FROM  Z_TypeEnvoiMail WHERE TE_Intitule = 'Stock épuisé')
    INSERT INTO Z_TypeEnvoiMail values('Stock épuisé');
IF NOT EXISTS (SELECT 1 FROM  Z_TypeEnvoiMail WHERE TE_Intitule = 'Stock cumulé')
    INSERT INTO Z_TypeEnvoiMail values('Stock cumulé');
IF NOT EXISTS (SELECT 1 FROM  Z_TypeEnvoiMail WHERE TE_Intitule = 'Modification prix article')
    INSERT INTO Z_TypeEnvoiMail values('Modification prix article');
IF NOT EXISTS (SELECT 1 FROM  Z_TypeEnvoiMail WHERE TE_Intitule = 'Suppression ligne facture')
    INSERT INTO Z_TypeEnvoiMail values('Suppression ligne facture');

IF COL_LENGTH('F_DOCLIGNE','USERGESCOM') IS NULL
ALTER TABLE F_DOCLIGNE ADD [USERGESCOM] [varchar](40) NULL;
IF COL_LENGTH('F_DOCLIGNE','NOMCLIENT') IS NULL
ALTER TABLE F_DOCLIGNE ADD 	[NOMCLIENT] [varchar](60) NULL;
IF COL_LENGTH('F_DOCLIGNE','DATEMODIF') IS NULL
ALTER TABLE F_DOCLIGNE ADD [DATEMODIF] [smalldatetime] NULL;
IF COL_LENGTH('F_DOCLIGNE','ORDONATEUR_REMISE') IS NULL
ALTER TABLE F_DOCLIGNE ADD [ORDONATEUR_REMISE] [varchar](69) NULL;
IF COL_LENGTH('F_DOCLIGNE','MACHINEPC') IS NULL
ALTER TABLE F_DOCLIGNE ADD 	[MACHINEPC] [varchar](69) NULL;
IF COL_LENGTH('F_DOCLIGNE','GROUPEUSER') IS NULL
ALTER TABLE F_DOCLIGNE ADD	[GROUPEUSER] [varchar](10) NULL;

IF COL_LENGTH('F_DOCENTETE','longitude') IS NULL
ALTER TABLE F_DOCENTETE ADD [longitude] [float] NULL;
IF COL_LENGTH('F_DOCENTETE','latitude') IS NULL
ALTER TABLE F_DOCENTETE ADD 	[latitude] [float] NULL;
IF COL_LENGTH('F_DOCENTETE','VEHICULE') IS NULL
ALTER TABLE F_DOCENTETE ADD 	[VEHICULE] [varchar](10) NULL;
IF COL_LENGTH('F_DOCENTETE','CHAUFFEUR') IS NULL
ALTER TABLE F_DOCENTETE ADD [CHAUFFEUR] [varchar](10) NULL;
GO

IF OBJECT_ID('[dbo].[Z_LIGNE_CONFIRMATION]', 'U') IS NULL
CREATE TABLE Z_LIGNE_CONFIRMATION (
                                      cbMarq INT IDENTITY(1, 1) PRIMARY KEY,
                                      AR_Ref VARCHAR(19),
                                      Prix FLOAT,
                                      DL_Qte FLOAT,
                                      cbMarqEntete INT,
                                      cbMarqLigneFirst INT
)

IF OBJECT_ID('[dbo].[Z_DEPOT_DETAIL]', 'U') IS NULL
CREATE TABLE Z_DEPOT_DETAIL (	DE_No INT NOT NULL,
                                 CA_CatTarif INT,
                                 cbMarq INT NOT NULL IDENTITY(1, 1) PRIMARY KEY)

IF OBJECT_ID('[dbo].[Z_CALENDAR_USER]', 'U') IS NULL
CREATE TABLE Z_CALENDAR_USER (
                                 PROT_No INT,
                                 ID_JourDebut INT,
                                 ID_JourFin INT,
                                 ID_HeureDebut INT,
                                 ID_MinDebut INT,
                                 ID_HeureFin INT,
                                 ID_MinFin INT)



IF OBJECT_ID('[dbo].[Z_LogInfo]', 'U') IS NULL

CREATE TABLE [dbo].[Z_LogInfo](
                                  [Action] [varchar](250) NULL
    ,[Type] [varchar](250) NULL
    ,[DoType] [int] NULL
    ,[DoEntete] [varchar](250) NULL
    ,[DE_No] [int] NULL
    ,[DoDomaine] [int] NULL
    ,[AR_Ref] [varchar](250) NULL
    ,[Qte] [float] NULL
    ,[Prix] [float] NULL
    ,[Remise] [varchar](250) NULL
    ,[Montant] [float] NULL
    ,[Date] [date] NULL
    ,[UserName] [varchar](250) NULL
    ,[cbMarq] int
    ,[tables] [varchar](30) NULL
    ,[cbCreateur] varchar(30) NULL
    ,[DateDocument] [date] NULL
    ,[cbMarqLog] INT NOT NULL IDENTITY(1, 1) PRIMARY KEY
)
IF COL_LENGTH('Z_LogInfo','cbMarqLog') IS NULL
ALTER TABLE [dbo].[Z_LogInfo] add [cbMarqLog] INT NOT NULL IDENTITY(1, 1) PRIMARY KEY


IF OBJECT_ID('[dbo].[Z_RGLT_COMPTEA]', 'U') IS NULL
CREATE TABLE [dbo].[Z_RGLT_COMPTEA](
                                       [RG_No] [int] NULL,
                                       [CA_Num] [varchar](20) NULL,
                                       cbMarq INT NOT NULL IDENTITY(1, 1) PRIMARY KEY
)

IF OBJECT_ID('[dbo].[Z_RGLT_VRSTBANCAIRE]', 'U') IS NULL
CREATE TABLE [dbo].[Z_RGLT_VRSTBANCAIRE](
                                            [RG_No] [int] NULL,
                                            [RG_NoCache] [int] NULL,
                                            cbMarq INT NOT NULL IDENTITY(1, 1) PRIMARY KEY
)

IF OBJECT_ID('[dbo].[Z_ProtUser]', 'U') IS NULL
CREATE TABLE dbo.Z_ProtUser (
                                PROT_No INT,
                                ProtectAdmin TINYINT
)

IF OBJECT_ID('[dbo].[Z_DocligneLivree]', 'U') IS NULL
CREATE TABLE [dbo].[Z_DocligneLivree](
                                         [cbMarqLigne] [int] NOT NULL,
                                         [AR_Ref] [varchar](70) NOT NULL,
                                         [DL_QteBL] [numeric](24, 6) NOT NULL,
                                         [DL_QteBLRestant] [numeric](24, 6) NOT NULL,
                                         [USER_GESCOM] [varchar](70) NOT NULL,
                                         [cbModification] [smalldatetime] NOT NULL,
                                         [cbMarq] [int] IDENTITY(1,1) NOT NULL
)

IF NOT EXISTS (SELECT 1 FROM  LIB_CMD WHERE Libelle_Cmd = 'Clôture de caisse')
    INSERT INTO dbo.LIB_CMD VALUES(34095,'Clôture de caisse',1,1113,0)


IF COL_LENGTH('Z_LogInfo','DateDocument') IS NULL
ALTER TABLE Z_LogInfo ADD DateDocument DATE

IF COL_LENGTH('F_DOCLIGNE','ORDONATEUR_REMISE') IS NULL
ALTER TABLE F_DOCLIGNE ADD ORDONATEUR_REMISE VARCHAR(69)

IF COL_LENGTH('F_DOCLIGNE','DL_SUPER_PRIX') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_SUPER_PRIX NUMERIC(24,6) NULL

IF COL_LENGTH('F_DOCLIGNE','DL_QTE_SUPER_PRIX') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_QTE_SUPER_PRIX NUMERIC(24,6) NULL

IF COL_LENGTH('F_DOCLIGNE','DL_COMM') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_COMM VARCHAR(69)

IF COL_LENGTH('F_DOCLIGNE','GROUPEUSER') IS NULL
ALTER TABLE F_DOCLIGNE ADD GROUPEUSER VARCHAR(10)


IF COL_LENGTH('F_DOCLIGNE','Qte_LivreeBL') IS NULL
ALTER TABLE F_DOCLIGNE ADD Qte_LivreeBL NUMERIC(24,6) NULL

IF COL_LENGTH('F_DOCLIGNE','Qte_RestantBL') IS NULL
ALTER TABLE F_DOCLIGNE ADD Qte_RestantBL NUMERIC(24,6) NULL
IF COL_LENGTH('F_ARTICLE','Prix_Min') IS NULL
ALTER TABLE F_ARTICLE ADD Prix_Min NUMERIC(24,6) NULL
IF COL_LENGTH('F_ARTICLE','Prix_Max') IS NULL
ALTER TABLE F_ARTICLE ADD Prix_Max NUMERIC(24,6) NULL
IF COL_LENGTH('F_ARTICLE','QTE_GROS') IS NULL
ALTER TABLE F_ARTICLE ADD QTE_GROS NUMERIC(24,6) NULL

IF COL_LENGTH('F_ARTICLE','LienImage') IS NULL
ALTER TABLE F_ARTICLE ADD LienImage NVARCHAR(500) NULL

IF COL_LENGTH('F_ARTICLE','PF') IS NULL
ALTER TABLE F_ARTICLE ADD PF INT NULL

IF COL_LENGTH('F_COMPTET','PF') IS NULL
ALTER TABLE F_COMPTET ADD PF INT NULL

IF COL_LENGTH('F_COMPTET','CumulPF') IS NULL
ALTER TABLE F_COMPTET ADD CumulPF NUMERIC(24,6) NULL

IF COL_LENGTH('F_COMPTET','PF_Consommer') IS NULL
ALTER TABLE F_COMPTET ADD PF_Consommer NUMERIC(24,6) NULL

IF COL_LENGTH('F_DOCLIGNE','CumulPF') IS NULL
ALTER TABLE F_DOCLIGNE ADD CumulPF NUMERIC(24,6) NULL

IF NOT EXISTS(SELECT R_CODE
              FROM P_REGLEMENT WHERE R_CODE = 'OM' AND 	R_Intitule ='ORANGE_MONEY')
    UPDATE	P_REGLEMENT
    SET	R_Intitule ='ORANGE_MONEY'
      ,R_CODE = '05'
    WHERE	cbIndice=5

IF NOT EXISTS(SELECT R_CODE
              FROM P_REGLEMENT WHERE R_CODE = 'MO' AND 	R_Intitule ='MTN_MONEY')
    UPDATE	P_REGLEMENT
    SET	R_Intitule ='MTN_MONEY'
      ,R_CODE = '06'
    WHERE	cbIndice=6

IF OBJECT_ID('[dbo].[Z_AutoMajCompta]', 'U') IS NULL
CREATE TABLE [dbo].[Z_AutoMajCompta](
                                        [TypeMajCompta] [int] NOT NULL,
                                        [Periode] [int] NOT NULL,
                                        [NomAction] VARCHAR(50) NOT NULL,
                                        [cbModification] [smalldatetime] NOT NULL,
                                        [Profondeur] [int] NOT NULL,
                                        [cbMarq] [int] IDENTITY(1,1) NOT NULL
)

IF OBJECT_ID('[dbo].[Z_ExecQuery]', 'U') IS NULL
CREATE TABLE [dbo].[Z_ExecQuery](
                                    [Query] [NVARCHAR](max) NOT NULL,
                                    [cbModification] [smalldatetime] NOT NULL,
                                    [cbMarq] [int] IDENTITY(1,1) NOT NULL
)
GO
IF COL_LENGTH('Z_LIGNE_CONFIRMATION','cbModification') IS NULL
ALTER TABLE Z_LIGNE_CONFIRMATION ADD cbModification DATETIME DEFAULT GETDATE();

IF COL_LENGTH('F_COMPTET','CodeInterne') IS NULL
ALTER TABLE F_COMPTET ADD CodeInterne INT NULL

IF COL_LENGTH('F_COMPTET','cbMarqLocal') IS NULL
ALTER TABLE F_COMPTET ADD cbMarqLocal BIGINT NULL

IF COL_LENGTH('F_ARTICLE','cbMarqLocal') IS NULL
ALTER TABLE F_ARTICLE ADD cbMarqLocal BIGINT NULL

IF COL_LENGTH('F_DOCENTETE','cbMarqLocal') IS NULL
ALTER TABLE F_DOCENTETE ADD cbMarqLocal BIGINT NULL

IF COL_LENGTH('F_DOCLIGNE','cbMarqLocal') IS NULL
ALTER TABLE F_DOCLIGNE ADD cbMarqLocal BIGINT NULL

IF COL_LENGTH('F_DOCREGL','cbMarqLocal') IS NULL
ALTER TABLE F_DOCREGL ADD cbMarqLocal BIGINT NULL

IF COL_LENGTH('F_REGLECH','cbMarqLocal') IS NULL
ALTER TABLE F_REGLECH ADD cbMarqLocal BIGINT NULL

IF COL_LENGTH('F_CREGLEMENT','cbMarqLocal') IS NULL
ALTER TABLE F_CREGLEMENT ADD cbMarqLocal BIGINT NULL

IF COL_LENGTH('Z_DEPOTCAISSE','cbModification') IS NULL
ALTER TABLE Z_DEPOTCAISSE ADD cbModification DATETIME DEFAULT GETDATE();

IF COL_LENGTH('Z_DEPOTUSER','cbModification') IS NULL
ALTER TABLE Z_DEPOTUSER ADD cbModification DATETIME DEFAULT GETDATE();

IF COL_LENGTH('Z_DEPOT_DETAIL','cbModification') IS NULL
ALTER TABLE Z_DEPOT_DETAIL ADD cbModification DATETIME DEFAULT GETDATE();

IF COL_LENGTH('Z_DEPOTSOUCHE','cbModification') IS NULL
ALTER TABLE Z_DEPOTSOUCHE ADD cbModification DATETIME DEFAULT GETDATE();

IF COL_LENGTH('Z_DocligneLivree','cbMarqLocal') IS NULL
ALTER TABLE Z_DocligneLivree ADD cbMarqLocal INT NULL;

IF OBJECT_ID('[dbo].[Z_LogArticle]', 'U') IS NULL
CREATE TABLE Z_LogArticle (
                              [cbMarq] [int] IDENTITY(1,1) NOT NULL,
                              [AR_Ref] NVARCHAR(50) NULL,
                              [AR_Design] NVARCHAR(350) NULL,
                              [APrix_Min] FLOAT NULL,
                              [Prix_Min] FLOAT NULL,
                              [APrix_Max] FLOAT NULL,
                              [Prix_Max] FLOAT NULL,
                              [cbCreateur] NVARCHAR(50) NULL,
                              [USERNAME] NVARCHAR(350) NULL,
                              [cbModification] [smalldatetime] NULL
)


IF COL_LENGTH('F_DOCLIGNE','DL_ComposantAjoute') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_ComposantAjoute NUMERIC(24,6)

IF COL_LENGTH('F_DOCLIGNE','DL_RendementProd') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_RendementProd NUMERIC(24,6)

GO

CREATE OR ALTER VIEW dbo.vWTypeDocument AS
WITH _TypeDocument_ AS (
    SELECT DO_Domaine = 0,DO_Type = 0,DO_Provenance = 0,TypeDocument = 'Devis',docCurrentType = 0
    UNION
    SELECT DO_Domaine = 0,DO_Type = 2,DO_Provenance = 0,TypeDocument = 'PreparationLivraison',docCurrentType = 0
    UNION
    SELECT DO_Domaine = 0,DO_Type = 1,DO_Provenance = 0,TypeDocument = 'BonCommande',docCurrentType = 0
    UNION
    SELECT DO_Domaine = 0,DO_Type = 3,DO_Provenance = 0,TypeDocument = 'BonLivraison',docCurrentType = 3
    UNION
    SELECT DO_Domaine = 0,DO_Type = 4,DO_Provenance = 0,TypeDocument = 'BonRetour',docCurrentType = 0
    UNION
    SELECT DO_Domaine = 0,DO_Type = 5,DO_Provenance = 0,TypeDocument = 'BonAvoir',docCurrentType = 0
    UNION
    SELECT DO_Domaine = 0,DO_Type = 6,DO_Provenance = 0,TypeDocument = 'Vente',docCurrentType = 6
    UNION
    SELECT DO_Domaine = 0,DO_Type = 6,DO_Provenance = 1,TypeDocument = 'VenteRetour',docCurrentType = 7
    UNION
    SELECT DO_Domaine = 0,DO_Type = 6,DO_Provenance = 2,TypeDocument = 'VenteAvoir',docCurrentType = 8
    UNION
    SELECT DO_Domaine = 0,DO_Type = 7,DO_Provenance = 0,TypeDocument = 'VenteC',docCurrentType = 6
    UNION
    SELECT DO_Domaine = 0,DO_Type = 7,DO_Provenance = 1,TypeDocument = 'VenteRetourC',docCurrentType = 7
    UNION
    SELECT DO_Domaine = 0,DO_Type = 7,DO_Provenance = 2,TypeDocument = 'VenteAvoirC',docCurrentType = 8
    UNION
    SELECT DO_Domaine = 1,DO_Type = 11,DO_Provenance = 0,TypeDocument = 'PreparationCommande',docCurrentType = 1
    UNION
    SELECT DO_Domaine = 1,DO_Type = 12,DO_Provenance = 0,TypeDocument = 'BonCommandeAchat',docCurrentType = 2
    UNION
    SELECT DO_Domaine = 1,DO_Type = 13,DO_Provenance = 0,TypeDocument = 'BonLivraisonAchat',docCurrentType = 3
    UNION
    SELECT DO_Domaine = 1,DO_Type = 14,DO_Provenance = 0,TypeDocument = 'BonRetourAchat',docCurrentType = 0
    UNION
    SELECT DO_Domaine = 1,DO_Type = 15,DO_Provenance = 0,TypeDocument = 'BonAvoirAchat',docCurrentType = 0
    UNION
    SELECT DO_Domaine = 1,DO_Type = 16,DO_Provenance = 0,TypeDocument = 'Achat',docCurrentType = 6
    UNION
    SELECT DO_Domaine = 1,DO_Type = 16,DO_Provenance = 1,TypeDocument = 'AchatRetour',docCurrentType = 7
    UNION
    SELECT DO_Domaine = 1,DO_Type = 16,DO_Provenance = 2,TypeDocument = 'AchatAvoir',docCurrentType = 8
    UNION
    SELECT DO_Domaine = 1,DO_Type = 17,DO_Provenance = 0,TypeDocument = 'AchatC',docCurrentType = 6
    UNION
    SELECT DO_Domaine = 1,DO_Type = 17,DO_Provenance = 1,TypeDocument = 'AchatRetourC',docCurrentType = 7
    UNION
    SELECT DO_Domaine = 1,DO_Type = 17,DO_Provenance = 2,TypeDocument = 'AchatAvoirC',docCurrentType = 8
    UNION
    SELECT DO_Domaine = 2,DO_Type = 20,DO_Provenance = 0,TypeDocument = 'Entree',docCurrentType = 0
    UNION
    SELECT DO_Domaine = 2,DO_Type = 21,DO_Provenance = 0,TypeDocument = 'Sortie',docCurrentType = 1
    UNION
    SELECT DO_Domaine = 2,DO_Type = 23,DO_Provenance = 0,TypeDocument = 'Transfert',docCurrentType = 3
    UNION
    SELECT DO_Domaine = 2,DO_Type = 24,DO_Provenance = 0,TypeDocument = 'PreparationFabrication',docCurrentType = 4
    UNION
    SELECT DO_Domaine = 2,DO_Type = 25,DO_Provenance = 0,TypeDocument = 'OrdreFabrication',docCurrentType = 5
    UNION
    SELECT DO_Domaine = 2,DO_Type = 26,DO_Provenance = 0,TypeDocument = 'BonFabrication',docCurrentType = 6
    UNION
    SELECT DO_Domaine = 3,DO_Type = 30,DO_Provenance = 0,TypeDocument = 'Ticket',docCurrentType = 6
    UNION
    SELECT DO_Domaine = 4,DO_Type = 40,DO_Provenance = 0,TypeDocument = 'Transfert_detail',docCurrentType = 0
    UNION
    SELECT DO_Domaine = 4,DO_Type = 41,DO_Provenance = 0,TypeDocument = 'Transfert_detail',docCurrentType = 0
    UNION
    SELECT DO_Domaine = 4,DO_Type = 44,DO_Provenance = 0,TypeDocument = 'Transfert_confirmation',docCurrentType = 4
    UNION
    SELECT DO_Domaine = 4,DO_Type = 44,DO_Provenance = 0,TypeDocument = 'Transfert_valid_confirmation',docCurrentType = 4
    UNION
    SELECT DO_Domaine = 4,DO_Type = 44,DO_Provenance = 0,TypeDocument = 'SortieInterne',docCurrentType = 4
    UNION
    SELECT DO_Domaine = 4,DO_Type = 45,DO_Provenance = 0,TypeDocument = 'DemandeEmission',docCurrentType = 5
)
SELECT *
     ,DO_Transaction = (SELECT CASE
                                   WHEN TypeDocument IN ('BonCommande', 'BonLivraison', 'Devis', 'Vente') THEN 11
                                   WHEN TypeDocument = 'Ticket' THEN 11
                                   WHEN TypeDocument IN ('VenteAvoir', 'VenteRetour', 'AchatRetour') THEN 21
                                   WHEN DO_Domaine = 1 THEN 11
                                   WHEN DO_Domaine = 2 THEN 0
                                   ELSE 0
                                   END)
     ,DO_Regime = (SELECT CASE
                              WHEN TypeDocument IN ('VenteAvoir', 'VenteRetour', 'AchatRetour') THEN 25
                              WHEN DO_Domaine = 1 THEN 11
                              WHEN TypeDocument IN ('BonCommande', 'BonLivraison', 'Devis', 'Vente', 'PreparationCommande') THEN 21
                              WHEN DO_Domaine = 2 THEN 0
                              ELSE 0
                              END)
     ,DO_Condition = CASE WHEN DO_Domaine = 2 THEN 0 ELSE 1 END
     ,DO_Period = CASE WHEN DO_Domaine = 2 THEN 0 ELSE 1 END
     ,DO_Expedit= CASE WHEN DO_Domaine = 2 THEN 0 ELSE 1 END
     ,DO_NbFacture= CASE WHEN DO_Domaine = 2 THEN 0 ELSE 1 END
FROM _TypeDocument_
GO
UPDATE cbSysLibre SET CB_Type = 9
WHERE CB_Name IN ('OCC_DEBUT','OCC_FIN')


IF OBJECT_ID('[dbo].[Z_LINKED_DB]', 'U') IS NULL
CREATE TABLE [dbo].[Z_LINKED_DB](
    [DB_NAME] [varchar](50) NULL,
    Z_LINKED_DB [varchar](50) NULL,
    [cbMarq] [int] IDENTITY(1,2) NOT NULL
)


IF OBJECT_ID('[dbo].[Z_ModeleReglement]', 'U') IS NULL
CREATE TABLE Z_ModeleReglement(
      [Intitule] VARCHAR(100)
    ,[CG_Num] VARCHAR(50)
    ,[JO_Num] VARCHAR(50)
    ,[CA_Num] VARCHAR(50)
    ,[CA_NumSec] VARCHAR(50)
    ,[RG_TypeReg] INT
    ,[CA_No] INT
    ,[CA_No_Dest] INT
    ,[RG_Type] INT
    ,[cbModification] smalldatetime
    ,[cbMarq]  INT IDENTITY(1,1) NOT NULL
)

IF COL_LENGTH('F_DOCLIGNE','DL_QteCond01') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_QteCond01 NUMERIC(24,6)

IF COL_LENGTH('F_DOCLIGNE','DL_PrixCond01') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_PrixCond01 NUMERIC(24,6)

IF COL_LENGTH('F_DOCLIGNE','DL_QteCond02') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_QteCond02 NUMERIC(24,6)

IF COL_LENGTH('F_DOCLIGNE','DL_PrixCond02') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_PrixCond02 NUMERIC(24,6)

IF COL_LENGTH('F_DOCLIGNE','DL_QteCond03') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_QteCond03 NUMERIC(24,6)

IF COL_LENGTH('F_DOCLIGNE','DL_PrixCond03') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_PrixCond03 NUMERIC(24,6)

IF COL_LENGTH('F_DOCLIGNE','DL_QteCond04') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_QteCond04 NUMERIC(24,6)

IF COL_LENGTH('F_DOCLIGNE','DL_PrixCond04') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_PrixCond04 NUMERIC(24,6)

IF COL_LENGTH('F_DOCLIGNE','DL_QteCond05') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_QteCond05 NUMERIC(24,6)

IF COL_LENGTH('F_DOCLIGNE','DL_PrixCond05') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_PrixCond05 NUMERIC(24,6)

IF COL_LENGTH('F_DOCLIGNE','DL_QteCond06') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_QteCond06 NUMERIC(24,6)

IF COL_LENGTH('F_DOCLIGNE','DL_PrixCond06') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_PrixCond06 NUMERIC(24,6)

IF COL_LENGTH('F_DOCLIGNE','DL_QteCond07') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_QteCond07 NUMERIC(24,6)

IF COL_LENGTH('F_DOCLIGNE','DL_PrixCond07') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_PrixCond07 NUMERIC(24,6)

IF COL_LENGTH('F_DOCLIGNE','DL_QteCond06') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_QteCond06 NUMERIC(24,6)

IF COL_LENGTH('F_DOCLIGNE','DL_PrixCond06') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_PrixCond06 NUMERIC(24,6)

IF COL_LENGTH('F_DOCLIGNE','DL_QteCond07') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_QteCond07 NUMERIC(24,6)

IF COL_LENGTH('F_DOCLIGNE','DL_PrixCond07') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_PrixCond07 NUMERIC(24,6)

IF COL_LENGTH('F_DOCLIGNE','DL_QteCond08') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_QteCond08 NUMERIC(24,6)

IF COL_LENGTH('F_DOCLIGNE','DL_PrixCond08') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_PrixCond08 NUMERIC(24,6)

IF COL_LENGTH('F_DOCLIGNE','DL_QteCond09') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_QteCond09 NUMERIC(24,6)

IF COL_LENGTH('F_DOCLIGNE','DL_PrixCond09') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_PrixCond09 NUMERIC(24,6)

IF COL_LENGTH('F_DOCLIGNE','DL_QteCond10') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_QteCond10 NUMERIC(24,6)

IF COL_LENGTH('F_DOCLIGNE','DL_PrixCond10') IS NULL
ALTER TABLE F_DOCLIGNE ADD DL_PrixCond10 NUMERIC(24,6)



IF OBJECT_ID('[dbo].[Z_LogInfoRglt]', 'U') IS NULL

CREATE TABLE [dbo].[Z_LogInfoRglt](
    [Action] [varchar](250) NULL
    ,[Type] [varchar](250) NULL
    ,[RG_No] [int] NULL
    ,[RG_Libelle] [varchar](250) NULL
    ,[CT_Num] [varchar](250) NULL
    ,[CA_No] [int] NULL
    ,[CO_NoCaissier] [int] NULL
    ,[RG_TypeReg] [int] NULL
    ,[JO_Num] [varchar](250) NULL
    ,[RG_Montant] [float] NULL
    ,[Date] [smalldatetime] NULL
    ,[UserName] [varchar](250) NULL
    ,[N_Reglement] [varchar](250) NULL
    ,[cbMarq] int
    ,[tables] [varchar](30) NULL
    ,[cbCreateur] varchar(30) NULL
    ,[DateDocument] [smalldatetime] NULL
    ,[cbMarqLog] INT NOT NULL IDENTITY(1, 1) PRIMARY KEY
)

IF OBJECT_ID('[dbo].[Z_LogInfoEntete]', 'U') IS NULL

CREATE TABLE [dbo].[Z_LogInfoEntete](
                                        [Action] [varchar](250) NULL
    ,[Type] [varchar](250) NULL
    ,[TypeFacture] [varchar](100) NULL
    ,[DO_Piece] [varchar](50) NULL
    ,[DO_Date] [smalldatetime] NULL
    ,[CA_Num] [varchar](50) NULL
    ,[PROT_No] [int] NULL
    ,[DO_Coord01] [varchar](250) NULL
    ,[DO_Coord02] [varchar](250) NULL
    ,[DO_Coord03] [varchar](250) NULL
    ,[DO_Coord04] [varchar](250) NULL
    ,[DO_Statut] [int] NULL
    ,[Latitude] [float] NULL
    ,[Longitude] [float] NULL
    ,[DE_No] [int] NULL
    ,[DO_Tarif] [int] NULL
    ,[N_CatCompta] [int] NULL
    ,[DO_Souche] [int] NULL
    ,[CA_No] [int] NULL
    ,[DO_Ref] [varchar](250) NULL
    ,[Date] [smalldatetime] NULL
    ,[cbMarq] int
    ,[tables] [varchar](30) NULL
    ,[cbCreateur] varchar(30) NULL
    ,[cbMarqLog] INT NOT NULL IDENTITY(1, 1) PRIMARY KEY
)

IF OBJECT_ID('[dbo].[Z_LogInfoArtStock]', 'U') IS NULL

CREATE TABLE [dbo].[Z_LogInfoArtStock](
    [Action] [varchar](250) NULL
    ,[Type] [varchar](250) NULL
    ,[TypeFacture] [varchar](100) NULL
    ,[DO_Piece] [varchar](50) NULL
    ,[DO_Date] [smalldatetime] NULL
    ,[cbMarqLigne] INT NULL
    ,[AR_Ref] varchar(250) NULL
    ,[DE_No] INT NULL
    ,[AS_MontStock] FLOAT NULL
    ,[AS_QteSto] FLOAT NULL
    ,[AS_QteRes] FLOAT NULL
    ,[AS_QteCom] FLOAT NULL
    ,[Date] [smalldatetime] NULL
    ,[tables] [varchar](30) NULL
    ,[cbCreateur] varchar(30) NULL
    ,[cbMarqLog] INT NOT NULL IDENTITY(1, 1) PRIMARY KEY
)

IF COL_LENGTH('Z_RGLT_COMPTEA','CA_NumSec') IS NULL
ALTER TABLE Z_RGLT_COMPTEA ADD CA_NumSec VARCHAR(69)

IF COL_LENGTH('Z_DEPOT_DETAIL','CT_NumAch') IS NULL
ALTER TABLE Z_DEPOT_DETAIL ADD CT_NumAch VARCHAR(69)

IF COL_LENGTH('Z_DEPOT_DETAIL','CT_NumVen') IS NULL
ALTER TABLE Z_DEPOT_DETAIL ADD CT_NumVen VARCHAR(69)

IF COL_LENGTH('F_CREGLEMENT','CG_Commentaire') IS NULL
ALTER TABLE F_CREGLEMENT ADD CG_Commentaire VARCHAR(300)

IF COL_LENGTH('F_CREGLEMENT','CG_Statut') IS NULL
ALTER TABLE F_CREGLEMENT ADD CG_Statut INT


IF COL_LENGTH('F_CREGLEMENT','CG_Statut') IS NULL
    BEGIN
        ALTER TABLE F_CREGLEMENT ADD CG_Statut NUMERIC(23,6)
        INSERT INTO cbSysLibre (CB_File,CB_Name,CB_Pos,CB_Type,CB_Len,CB_Flag,CB_Formule,CB_Creator)
        SELECT 'F_CREGLEMENT','CG_Statut',(SELECT MAX(CB_Pos) FROM cbSysLibre)+1,7,8,0,NULL,NULL
    END

GO


CREATE OR ALTER  FUNCTION ImpactArtStock (
    @dlTNomencl INT,
    @typeFac VARCHAR(100),
    @arSuiviStock INT,
    @DL_MvtStock INT
)
    RETURNS TABLE
        AS
        RETURN
         SELECT [ImpactAsQteSto] = CASE WHEN @dlTNomencl = 0 AND @typeFac IN ('Vente','Ticket','BonLivraison','VenteRetour','Achat','AchatRetour','BonFabrication','Entree','Sortie','Transfert','Transfert_confirmation','Transfert_detail')  AND @arSuiviStock = 2 AND @DL_MvtStock <> 0 THEN 1
                                        WHEN @dlTNomencl = 1 AND @typeFac IN('BonFabrication','OrdreFabrication') THEN 1
                                        ELSE 0 END
              ,[ImpactAsMontSto] = CASE WHEN @dlTNomencl = 0 AND @typeFac IN('Vente','Ticket','BonLivraison','VenteRetour','Achat','AchatRetour','AchatAvoir','BonFabrication','Entree','Sortie','Transfert','Transfert_confirmation','Transfert_detail') THEN 1
                                        WHEN @dlTNomencl = 1 AND @typeFac IN('BonFabrication','OrdreFabrication') THEN 1
                                        ELSE 0 END
              ,[ImpactAsQteCom] = CASE WHEN @dlTNomencl = 0 AND @typeFac IN('PreparationCommande','BonCommandeAchat','PreparationFabrication','OrdreFabrication') THEN 1
                                       ELSE 0 END
              ,[ImpactAsQteRes] = CASE WHEN @dlTNomencl = 0 AND @typeFac IN('BonCommande') THEN 1
                                       ELSE 0 END
              ,[sens] = CASE WHEN @typeFac IN('BonCommandeAchat','BonCommande','BonRetour','VenteRetour','PreparationCommande','BonLivraisonAchat','Achat','BonFabrication','PreparationFabrication','OrdreFabrication') THEN 1
                             WHEN @typeFac IN('Vente','BonLivraison','Ticket','AchatRetour','AchatAvoir') THEN -1
                             WHEN @typeFac IN('Devis','PreparationLivraison','VenteAvoir') THEN 0
                             WHEN @DL_MvtStock = 1 THEN 1
                             WHEN @DL_MvtStock = 3 THEN -1
             END;

GO

CREATE OR ALTER FUNCTION dbo.GetDOPiece
(
    @typeFacture VARCHAR(50),
    @doSouche INT
)
    RETURNS VARCHAR(20)
AS
BEGIN
    DECLARE @doccurent_type INT
    DECLARE @letterEntete VARCHAR(20)
    DECLARE @numberEntete VARCHAR(20)
    DECLARE @pattern VARCHAR(20)
    DECLARE @doPiece VARCHAR(20)
    DECLARE @doDomaine INT
    DECLARE @doProvenance INT
    DECLARE @doType INT
    DECLARE @nb INT = 0;
    DECLARE @nbExists INT = 0;

    SELECT	@doDomaine = DO_Domaine
         ,@doType = DO_Type
         ,@doccurent_type = docCurrentType
         ,@doProvenance = DO_Provenance
    FROM	vWTypeDocument
    WHERE	TypeDocument = @typeFacture

    SELECT @doPiece = ISNULL((SELECT DC_Piece
                              from F_DOCCURRENTPIECE D
                              WHERE DC_Domaine=@doDomaine AND DC_Souche=@doSouche AND DC_IdCol=@doccurent_type),0);

    SELECT @pattern = LEFT(@doPiece,PATINDEX('%[0-9]%', @doPiece)-1);

    WITH _DocEntete_ AS (
        SELECT DO_Domaine ,DO_Type ,DO_Piece,DO_Provenance,Nouveau = 0
        FROM VwDocEntete
        WHERE	(CASE WHEN @doType <>30 AND DO_Piece = @doPiece AND @doPiece <> '' THEN 1
                       WHEN @doType <>30 AND DO_Piece > @doPiece THEN 1
                       WHEN @doType <>30 AND TRY_CAST(@doPiece AS int) IS NOT NULL THEN 1
                       WHEN @doType = 30 AND DO_Type = @doType THEN 1 END) = 1
          AND (CASE WHEN @doDomaine = 2 AND DO_Domaine IN (2,4) THEN 1
                    WHEN @doDomaine <> 2 AND DO_Domaine=@doDomaine THEN 1 END) = 1
          AND     (CASE WHEN @doType=6 AND DO_Type IN(6,7) THEN 1
                        WHEN @doType=16 AND DO_Type IN(16,17) THEN 1
                        WHEN DO_Type NOT IN (16,6) AND @doType=DO_Type THEN 1 END) = 1
          AND DO_Provenance = @doProvenance
          AND CASE WHEN DO_Type <> 30 AND DO_Piece LIKE CONCAT(@pattern,'%') THEN 1
                   WHEN DO_Type = 30 AND TRY_CAST(DO_Piece AS INT) IS NOT NULL THEN 1
                  END = 1
        UNION
        SELECT @doDomaine , @doType , @doPiece,@doProvenance, Nouveau = 1
    )
    SELECT	@nb= COUNT(1)
         ,@doPiece = CASE WHEN @doType = 30 THEN CAST(MAX(TRY_CAST(DO_Piece AS INT)) AS VARCHAR(50))
                          WHEN @doType <> 30 AND CAST(SUBSTRING(MAX(DO_Piece), PATINDEX('%[0-9]%', MAX(DO_Piece)), LEN(MAX(DO_Piece))) AS INT) >  CAST(SUBSTRING(@doPiece, PATINDEX('%[0-9]%', @doPiece), LEN(@doPiece)) AS INT) THEN MAX(DO_Piece)
                          WHEN @doType <> 30 AND TRY_CAST(@doPiece AS int) IS NOT NULL THEN CAST(MAX(TRY_CAST(DO_Piece AS INT)) AS VARCHAR) ELSE @doPiece END
    FROM    _DocEntete_ ;


    WITH _DocEntete_ AS (
        SELECT  DO_Domaine
                ,DO_Type
                ,DO_Piece
                ,DO_Provenance
                ,Nouveau = 0
        FROM VwDocEntete
        WHERE	(CASE WHEN @doType <>30 AND DO_Piece = @doPiece AND @doPiece <> '' THEN 1
                       WHEN @doType <>30 AND DO_Piece > @doPiece THEN 1
                       WHEN @doType <>30 AND TRY_CAST(@doPiece AS int) IS NOT NULL THEN 1
                       WHEN @doType = 30 THEN 1 END) = 1
          AND (CASE WHEN @doDomaine = 2 AND DO_Domaine IN (2,4) THEN 1
                    WHEN @doDomaine <> 2 AND DO_Domaine=@doDomaine THEN 1 END) = 1
          AND     (CASE WHEN @doType=6 AND DO_Type IN(6,7) THEN 1
                        WHEN @doType=16 AND DO_Type IN(16,17) THEN 1
                        WHEN DO_Type NOT IN (16,6) AND @doType=DO_Type THEN 1 END) = 1
          AND DO_Provenance = @doProvenance
          AND DO_Piece LIKE CONCAT(@pattern,'%')
        UNION
        SELECT  @doDomaine
                ,@doType
                ,@doPiece
                ,@doProvenance
                ,Nouveau = 1
    )
    SELECT @nbExists = COUNT(1)
    FROM VwDocEntete docE
             INNER JOIN _DocEntete_ docEP ON docE.DO_Domaine = docEP.DO_Domaine
        AND  docE.DO_Type = docEP.DO_Type
        AND  docE.DO_Piece = docEP.DO_Piece
        AND  docE.DO_Provenance = docEP.DO_Provenance
        AND docEP.Nouveau = 1
    ;

    IF @nb = 0 OR @nbExists = 0
        SELECT @doPiece = ISNULL(@doPiece,1)
    ELSE
        BEGIN

            SELECT	@numberEntete = SUBSTRING(@doPiece, PATINDEX('%[0-9]%', @doPiece), LEN(@doPiece))
                 ,@letterEntete = SUBSTRING(@doPiece, 0, (LEN(@doPiece) - LEN(@numberEntete))+1);
            WITH _DocEntete_ AS (
                SELECT DO_Domaine ,DO_Type ,DO_Piece
                FROM VwDocEntete
                WHERE	DO_Piece LIKE CONCAT(@letterEntete,'%')
                  AND (CASE WHEN @doDomaine = 2 AND DO_Domaine IN (2,4) THEN 1
                            WHEN @doDomaine <> 2 AND DO_Domaine=@doDomaine THEN 1 END) = 1
                  AND     (CASE WHEN @doType=6 AND DO_Type IN(6,7) THEN 1
                                WHEN @doType=16 AND DO_Type IN(16,17) THEN 1
                                WHEN DO_Type NOT IN (16,6) AND @doType=DO_Type THEN 1 END) = 1
                  AND DO_Provenance = @doProvenance
                  AND DO_Piece LIKE CONCAT(@pattern,'%')
                UNION
                SELECT @doDomaine , @doType , @doPiece
            )
            SELECT	@letterEntete = ISNULL(CASE WHEN @doType = 30 THEN CAST(MAX(TRY_CAST(DO_Piece AS INT)) AS VARCHAR(50))
                                                  WHEN MAX(TRY_CAST(DO_Piece AS INT)) IS NOT NULL THEN CAST(MAX(TRY_CAST(DO_Piece AS INT)) AS VARCHAR) ELSE MAX(DO_Piece) END,@letterEntete)
                 ,@numberEntete = MAX(CAST(SUBSTRING(DO_Piece, PATINDEX('%[0-9]%',DO_Piece), LEN(DO_Piece)) AS INT))
            FROM    _DocEntete_

            SELECT	@numberEntete = CAST(@numberEntete AS INT)+1
                 ,@letterEntete = CASE WHEN (LEN(@letterEntete) - LEN(@numberEntete))+1 < 0 THEN '0' ELSE SUBSTRING(@letterEntete, 0, (LEN(@letterEntete) - LEN(@numberEntete))+1) END

            SELECT @doPiece = CONCAT(LEFT(CONCAT(@letterEntete,'000000000'),9-LEN(@numberEntete)),@numberEntete)


        END

    RETURN @doPiece

END;

GO
IF OBJECT_ID('[dbo].[Z_USERDETAIL]', 'U') IS NULL
CREATE TABLE [dbo].[Z_USERDETAIL](
                                     [Prot_No] [int] NOT NULL,
                                     [caNum] [int] NOT NULL
) ON [PRIMARY]

GO



IF COL_LENGTH('F_COMPTEA','CG_Num') IS NULL
ALTER TABLE F_COMPTEA ADD CG_Num VARCHAR(69)

IF COL_LENGTH('F_DOCLIGNE','DL_PoidsNetUnitaire') IS NULL
    BEGIN
        ALTER TABLE F_DOCLIGNE ADD DL_PoidsNetUnitaire NUMERIC(23,6)
        INSERT INTO cbSysLibre (CB_File,CB_Name,CB_Pos,CB_Type,CB_Len,CB_Flag,CB_Formule,CB_Creator)
        SELECT 'F_DOCLIGNE','DL_PoidsNetUnitaire',(SELECT MAX(CB_Pos) FROM cbSysLibre)+1,7,8,0,NULL,NULL
    END

IF COL_LENGTH('F_DOCLIGNE','DL_PoidsBrutUnitaire') IS NULL
    BEGIN
        ALTER TABLE F_DOCLIGNE ADD DL_PoidsBrutUnitaire NUMERIC(23,6)
        INSERT INTO cbSysLibre (CB_File,CB_Name,CB_Pos,CB_Type,CB_Len,CB_Flag,CB_Formule,CB_Creator)
        SELECT 'F_DOCLIGNE','DL_PoidsBrutUnitaire',(SELECT MAX(CB_Pos) FROM cbSysLibre)+1,7,8,0,NULL,NULL
    END

IF COL_LENGTH('F_DOCLIGNE','cbMarqBL') IS NULL
BEGIN
    ALTER TABLE F_DOCLIGNE ADD cbMarqBL NUMERIC(23,6)
END

IF COL_LENGTH('F_DOCLIGNE','cbMarqBC') IS NULL
BEGIN
    ALTER TABLE F_DOCLIGNE ADD cbMarqBC NUMERIC(23,6)
END

IF COL_LENGTH('F_DOCLIGNE','cbMarqPL') IS NULL
BEGIN
    ALTER TABLE F_DOCLIGNE ADD cbMarqPL NUMERIC(23,6)
END
GO
-- Index de synchronisation : retrouve une ligne reçue par sa base source et son cbMarq d'origine
IF COL_LENGTH('F_DOCLIGNE','DataBaseSource') IS NOT NULL AND COL_LENGTH('F_DOCLIGNE','cbMarqSource') IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_F_DOCLIGNE_SOURCE' AND object_id = OBJECT_ID('F_DOCLIGNE'))
    CREATE NONCLUSTERED INDEX IX_F_DOCLIGNE_SOURCE ON F_DOCLIGNE (DataBaseSource, cbMarqSource)
GO
