import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.*;

public class SendFileFTP {

    public static void main(String[] args) {
        JSch jsch = new JSch();
        Session session;
        String databaseSourceFile = "resource/databaseSource.json";
        if (args.length > 0)
            databaseSourceFile = args[0];
        JSONArray listObject = DataBase.getInfoConnexion(databaseSourceFile);
        if (listObject != null) {
            for (Object o : listObject) {
                JSONObject list = (JSONObject) o;

                if (list.get("envoi").equals("1") && list.get("active").equals("1")) {
                    try {
                        //session = jsch.getSession("u85460117-upload", "home631778145.1and1-data.host", 22);
                        session = jsch.getSession("u94723657", "home751918776.1and1-data.host", 22);
                        session.setPassword("ilIBWTvZme4CZD5BzMez");
                        //session.setPassword("FyK6cIAgpeNOSbEdfrpC*");
                        session.setConfig("StrictHostKeyChecking", "no");
                        session.connect();

                        Channel channel = session.openChannel("sftp");
                        channel.connect();
                        ChannelSftp sftpChannel = (ChannelSftp) channel;
                        File dir = new File((String) list.get("path"));

                        File[] directoryListing = dir.listFiles();
                        // folderftp peut contenir plusieurs dossiers séparés par ";" (un par client)
                        for (String folder : ((String) list.get("folderftp")).split(";")) {
                            folder = folder.trim();
                            if (folder.isEmpty())
                                continue;
                            try {
                                sftpChannel.mkdir(folder);
                            } catch (SftpException e) {
                                // dossier déjà existant
                            }
                            if (directoryListing != null) {
                                for (File child : directoryListing) {
                                    if (child.isFile()) {
                                        sftpChannel.put(child.getAbsolutePath(), "/" + folder + "/" + child.getName());
                                    }
                                }
                            }
                        }
                        File backup = new File(list.get("path") + "/archive");
                        if (!backup.isDirectory())
                            backup.mkdir();
                        if (directoryListing != null) {
                            for (File child : directoryListing) {
                                if (child.isFile()) {
                                    Table.archiveDocument(list.get("path") + "\\archive", (String) list.get("path"), child.getName());
                                }
                            }
                        }
                        sftpChannel.exit();
                        session.disconnect();
                    } catch (JSchException | SftpException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
