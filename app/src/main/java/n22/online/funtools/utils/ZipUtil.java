package n22.online.funtools.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Zip操作工具类
 *
 * @author Hugo
 */
public class ZipUtil {

    private static ZipUtil zipUtil;

    static final int BUFFER = 2048;

    private ZipUtil() {
    }

    public static ZipUtil newInstance() {
        if (zipUtil == null) {
            zipUtil = new ZipUtil();
        }
        return zipUtil;
    }

    /**
     * 解压
     *
     * @param fileName 加压zip名称(包含路径)
     * @param unZipDir 解压到的目录
     * @author Hugo
     */
    public static boolean unZip(String fileName, String unZipDir) {

        boolean flag = false;
        BufferedOutputStream dest = null;
        BufferedInputStream is = null;
        ZipEntry entry = null;
        ZipFile zipfile = null;
        FileOutputStream fos = null;

        try {
            // 先判断目标文件夹是否存在，如果不存在则新建，如果父目录不存在也新建
            File f = new File(unZipDir);
            if (!f.exists()) {
                f.mkdirs();
            }

            zipfile = new ZipFile(fileName);
            Enumeration<? extends ZipEntry> e = zipfile.entries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                if (entry.isDirectory()) {// 如果是目录，创建目录
                    makeDir(unZipDir + File.separator + entry.getName());
                } else {
                    makeDir(unZipDir + entry.getName().substring(0, entry.getName().lastIndexOf("/")));
                    is = new BufferedInputStream(zipfile.getInputStream(entry));
                    int count;
                    byte data[] = new byte[BUFFER];
                    fos = new FileOutputStream(unZipDir + File.separator + entry.getName());
                    dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = is.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();
                    is.close();
                }
            }
            zipfile.close();
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dest != null) {
                try {
                    dest.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (zipfile != null) {
                try {
                    zipfile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return flag;
    }

    /**
     * 创建目录
     *
     * @param unZipDir
     * @return
     * @author Hugo
     */
    private static boolean makeDir(String unZipDir) {
        boolean b = false;
        try {
            File f = new File(unZipDir);
            if (!f.exists()) {
                b = f.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return b;
        }
        return b;
    }

    /**
     * 对目录进行删除
     *
     * @param dir
     * @return
     * @author Hugo
     */
    public static boolean deleteDir(String dir) {
        boolean flag = false;

        File file = new File(dir);
        if (file.exists()) {
            flag = file.delete();
        } else {
            flag = true;
        }

        return flag;
    }

}
