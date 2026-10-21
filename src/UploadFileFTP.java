import com.jcraft.jsch.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.util.Vector;

public class UploadFileFTP {

    public static void main(String[] args) {
        JSch jsch = new JSch();
        Session session;
        String databaseSourceFile = "resource/databaseSource.json";
        if (args.length > 0)
            databaseSourceFile = args[0];

        JSONArray listObject = DataBase.getInfoConnexion(databaseSourceFile);
        try {
            session = jsch.getSession("u94723657", "home751918776.1and1-data.host", 22);
            session.setPassword("ilIBWTvZme4CZD5BzMez");

            //session = jsch.getSession("u85460117-upload", "home631778145.1and1-data.host", 22);
            session.setConfig("StrictHostKeyChecking", "no");
            //session.setPassword("FyK6cIAgpeNOSbEdfrpC*");
            session.connect();
        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();
        if (listObject != null) {
            for (Object o : listObject) {
                JSONObject list = (JSONObject) o;
                if (list.get("reception").equals("1") && list.get("active").equals("1")) {

                    // Check if folder exists
                    Vector<ChannelSftp.LsEntry> entries = sftpChannel.ls("/");

                    for (ChannelSftp.LsEntry entry : entries) {
                        if (entry.getAttrs().isDir() && entry.getFilename().equals(list.get("folderftp"))) {
                            System.out.println((String) list.get("folderftp"));
                            File dir = new File((String) list.get("path"));
                            sftpChannel.cd((String) list.get("folderftp"));

                            Vector<ChannelSftp.LsEntry> listFiles = sftpChannel.ls("*.zip");
                            for (ChannelSftp.LsEntry entryFiles : listFiles) {
                                if (!(dir).isDirectory())
                                    dir.mkdir();
                                System.out.println(entryFiles);
                                sftpChannel.get(entryFiles.getFilename(), dir.getAbsolutePath() + "\\" + entryFiles.getFilename());
                                sftpChannel.rm(entryFiles.getFilename());
                            }
                        }
                        sftpChannel.cd("..");
                    }
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
