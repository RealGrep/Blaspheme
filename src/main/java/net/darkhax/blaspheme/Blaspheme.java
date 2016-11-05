package net.darkhax.blaspheme;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public final class Blaspheme {
    
    /**
     * The current version of the library. Follows a Major-Release-Build structure. The major
     * number points to the current iteration of the project. The release number points to the
     * current release of the project. The build number refers to the current build of the
     * project and is handled by the build server.
     */
    public static final String VERSION = "1.0.0";
    
    /**
     * Instance of GSON, used for reading json files for pack downloads.
     */
    public static final Gson GSON = new Gson();
    
    /**
     * Pattern for verifying a URL.
     */
    public static final Pattern FILE_NAME_URL_PATTERN = Pattern.compile(".*?/([^/]*)$");
    
    /**
     * Logger for Blaspheme. Allows for greater compatibility support with other logger APIs.
     * This should only ever be used internally!
     */
    public static final Logger LOGGER = Logger.getLogger("Blaspheme");
    
    /**
     * Downloads a modpack from a curse URL.
     * 
     * @param url The URL of the modpack to download. Must point to curse, or a similar
     *        location.
     * @param setupMultiMC Should a MultiMC instance also be created?
     */
    public static void downloadModPackFromURL (String url, boolean setupMultiMC) throws Exception {
        
        LOGGER.log(Level.INFO, "Starting download for " + url);
        
        String packUrl = url;
        
        if (packUrl.endsWith("/"))
            packUrl = packUrl.replaceAll(".$", "");
        
        final String fileUrl = packUrl + "/files/latest";
        final String finalUrl = getLocationHeader(fileUrl);
        final Matcher matcher = FILE_NAME_URL_PATTERN.matcher(finalUrl);
        
        if (matcher.matches()) {
            
            final String packName = matcher.group(1);
            
            LOGGER.log(Level.INFO, "Modpack filename is " + packName);
            
            final File packMetaZip = getModpackData(packName, finalUrl);
            final Manifest manifest = readManifest(packMetaZip);
            final File outputDir = getOutputDir(packName);
            
            final File minecraftOutputDir = new File(outputDir, "minecraft");
            
            if (!minecraftOutputDir.exists())
                minecraftOutputDir.mkdir();
            
            downloadModpackFromManifest(minecraftOutputDir, manifest);
            copyOverrides(manifest, packMetaZip, minecraftOutputDir);
            
            if (setupMultiMC)
                setupMultimcInfo(manifest, outputDir);
            
            LOGGER.log(Level.INFO, "Pack downloaded sucessfully!");
        }
    }
    
    /**
     * Downloads a modpack manifest/meta file.
     * 
     * @param filename The intended name for the downloaded file.
     * @param url The URL to read the file from.
     * @return The resulting data file.
     */
    public static File getModpackData (String filename, String url) throws IOException, ZipException {
        
        final File tempPackDir = new File(getTempDir("blaspheme_temp"), filename);
        
        if (!tempPackDir.exists())
            tempPackDir.mkdir();
        
        final String zipName = filename.endsWith(".zip") ? filename : filename + ".zip";
        final String retPath = tempPackDir.getAbsolutePath();
        
        tempPackDir.deleteOnExit();
        
        LOGGER.log(Level.INFO, "Downloading pack data!");
        
        final File packDataFile = new File(tempPackDir, zipName);
        downloadFileFromURL(packDataFile, new URL(url));
        
        final ZipFile zip = new ZipFile(packDataFile);
        zip.extractAll(retPath);
        
        return tempPackDir;
    }
    
    /**
     * Gets a temporary directory which can be used to download files. This directory is
     * located within the user's home directory.
     * 
     * @param directoryName The name of the temporary directory.
     * @return A temporary directory which you can use.
     */
    public static File getTempDir (String directoryName) {
        
        final String home = System.getProperty("user.home");
        final File homeDir = new File(home, "/." + directoryName);
        
        LOGGER.log(Level.INFO, "Temp directory is " + homeDir);
        
        if (!homeDir.exists())
            homeDir.mkdir();
        
        return homeDir;
    }
    
    /**
     * Reads a file, searching for the pack manifest. Manifest must use manifest.json for the
     * file name.
     * 
     * @param dir The base directory to search within.
     * @return The manifest that was read.
     */
    public static Manifest readManifest (File dir) throws IOException {
        
        final File manifestJson = new File(dir, "manifest.json");
        
        if (!manifestJson.exists())
            throw new IllegalArgumentException("This modpack has no manifest");
        
        LOGGER.log(Level.INFO, "Reading pack manifest");
        
        return GSON.fromJson(new FileReader(manifestJson), Manifest.class);
    }
    
    /**
     * Reads a manifest object, and downloads all of the mods it contains.
     * 
     * @param outputDir The output directory for all the downloaded mods.
     * @param manifest The manifest to read from.
     * @return The output directory.
     */
    public static File downloadModpackFromManifest (File outputDir, Manifest manifest) throws IOException, URISyntaxException {
        
        final int total = manifest.files.size();
        
        LOGGER.log(Level.INFO, "Downloading pack from manifest!");
        LOGGER.log(Level.INFO, "Manifest contains " + total + " files to download");
        
        final File modsDir = new File(outputDir, "mods");
        
        if (!modsDir.exists())
            modsDir.mkdir();
        
        for (final Manifest.FileData targetFile : manifest.files)
            downloadModFile(targetFile, modsDir);
        
        LOGGER.log(Level.INFO, "Mod downloads complete");
        
        return outputDir;
    }
    
    /**
     * Copies all of the overrides for a modpack. These are for things like configs and
     * scripts. Basically everything that is not a mod.
     * 
     * @param manifest The manifest for the modpack.
     * @param tempDir The temporary download location.
     * @param outDir The propper output directory.
     */
    public static void copyOverrides (Manifest manifest, File tempDir, File outDir) throws IOException {
        
        LOGGER.log(Level.INFO, "Setting up overrides.");
        final File overridesDir = new File(tempDir, manifest.overrides);
        
        Files.walk(overridesDir.toPath()).forEach(path -> {
            
            try {
                
                final File override = path.toFile();
                
                if (override.isFile())
                    LOGGER.info("Copying " + path.getFileName());
                
                Files.copy(path, Paths.get(path.toString().replace(overridesDir.toString(), outDir.toString())));
            }
            
            catch (final IOException e) {
                
            }
        });
        
        LOGGER.log(Level.INFO, "Overides completed!");
    }
    
    /**
     * Creates a MultiMC instance file for a modpack.
     * 
     * @param manifest The manifest for the pack.
     * @param outputDir The propper output directory.
     */
    public static void setupMultimcInfo (Manifest manifest, File outputDir) throws IOException {
        
        LOGGER.log(Level.INFO, "Creating MultiMC info.");
        
        final File cfg = new File(outputDir, "instance.cfg");
        
        if (!cfg.exists())
            cfg.createNewFile();
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(cfg))) {
            
            writer.write("InstanceType=OneSix\n" + "IntendedVersion=" + manifest.minecraft.version + "\n" + "LogPrePostOutput=true\n" + "OverrideCommands=false\n" + "OverrideConsole=false\n" + "OverrideJavaArgs=false\n" + "OverrideJavaLocation=false\n" + "OverrideMemory=false\n" + "OverrideWindow=false\n" + "iconKey=default\n" + "lastLaunchTime=0\n" + "name=" + manifest.name + " " + manifest.version + "\n" + "notes=Modpack by " + manifest.author + ". Generated by CMPDL. Using Forge " + manifest.getForgeVersion() + ".\n" + "totalTimePlayed=0\n");
        }
    }
    
    /**
     * Creates the actual output for the modpack downloads. This is not temporary!
     * 
     * @param packName The name of the pack being downloaded. Used as the output name.
     * @return The actual output directory.
     */
    public static File getOutputDir (String packName) throws IOException, URISyntaxException {
        
        final File jarFile = new File(Blaspheme.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        final String homePath = jarFile.getParentFile().getAbsolutePath();
        
        String outname = URLDecoder.decode(packName, "UTF-8");
        outname = outname.replaceAll(".zip", "");
        final File outDir = new File(homePath, outname);
        
        LOGGER.log(Level.INFO, "Output directory is " + outDir);
        
        if (!outDir.exists())
            outDir.mkdir();
        
        return outDir;
    }
    
    /**
     * Downloads a mod file from a manifest entry.
     * 
     * @param file The data used to download the mod file.
     * @param modsDir The directory to download the file to.
     */
    public static void downloadModFile (Manifest.FileData file, File modsDir) throws IOException, URISyntaxException {
        
        final String baseUrl = "http://minecraft.curseforge.com/projects/" + file.projectID;
        
        String projectUrl = getLocationHeader(baseUrl);
        projectUrl = projectUrl.replaceAll("\\?cookieTest=1", "");
        
        final String fileDlUrl = projectUrl + "/files/" + file.fileID + "/download";
        final String finalUrl = getLocationHeader(fileDlUrl);
        final Matcher m = FILE_NAME_URL_PATTERN.matcher(finalUrl);
        
        if (!m.matches())
            throw new IllegalArgumentException("Mod file doesn't match filename pattern");
        
        String filename = m.group(1);
        filename = URLDecoder.decode(filename, "UTF-8");
        
        if (filename.endsWith("cookieTest=1"))
            LOGGER.log(Level.WARNING, "Missing file, it will be skipped!");
        
        else {
            
            LOGGER.log(Level.INFO, "Downloading " + filename);
            
            final File modFile = new File(modsDir, filename);
            
            if (modFile.exists())
                LOGGER.log(Level.INFO, "The mod " + modFile.getName() + " already exists. It will not be downloaded");
            
            else
                downloadFileFromURL(modFile, new URL(finalUrl));
        }
    }
    
    /**
     * Gets the header for a CurseForge url. Used to get project links from their modid.
     * 
     * @param targetURL The target url for the project.
     * @return The full URL for the project, including the header.
     */
    public static String getLocationHeader (String targetURL) throws IOException, URISyntaxException {
        
        URI uri = new URI(targetURL);
        HttpURLConnection connection = null;
        
        for (;;) {
            
            final URL url = uri.toURL();
            
            connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            String redirectLocation = connection.getHeaderField("Location");
            
            if (redirectLocation == null)
                break;
            
            redirectLocation = redirectLocation.replaceAll("\\[", "%5B");
            redirectLocation = redirectLocation.replaceAll("\\]", "%5D");
            
            if (redirectLocation.startsWith("/"))
                uri = new URI(uri.getScheme(), uri.getHost(), redirectLocation, uri.getFragment());
            
            else
                uri = new URI(redirectLocation);
        }
        
        return uri.toString();
    }
    
    /**
     * Downloads a file from the interwebs.
     * 
     * @param file The target location for the downloaded file.
     * @param url The URL to download the file from.
     * @throws IOException
     */
    public static void downloadFileFromURL (File file, URL url) throws IOException {
        
        if (!file.exists())
            file.createNewFile();
        
        try (InputStream instream = url.openStream(); FileOutputStream outStream = new FileOutputStream(file)) {
            
            final byte[] buff = new byte[4096];
            
            int i;
            
            while ((i = instream.read(buff)) > 0)
                outStream.write(buff, 0, i);
        }
    }
}
