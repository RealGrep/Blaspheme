package net.darkhax.blaspheme;

import java.util.List;

import net.darkhax.blaspheme.Manifest.MinecraftData.Modloader;

/**
 * Class representation of a curse project manifest.
 */
public class Manifest {
    
    /**
     * Info about the expected/target MC instance.
     */
    public MinecraftData minecraft;
    
    /**
     * The type of manifest file.
     */
    public String manifestType;
    
    /**
     * Version of the manifest file.
     */
    public String manifestVersion;
    
    /**
     * Name of the modpack.
     */
    public String name;
    
    /**
     * Version of the modpack.
     */
    public String version;
    
    /**
     * Author(s) of the pack.
     */
    public String author;
    
    /**
     * ID for the modpack project.
     */
    public int projectID;
    
    /**
     * List of all the mod files.
     */
    public List<FileData> files;
    
    /**
     * The overrides directory as a string.
     */
    public String overrides;
    
    /**
     * Gets the expected forge version for the pack.
     * 
     * @return The expected forge version as a string.
     */
    public String getForgeVersion () {
        
        for (final Modloader loader : this.minecraft.modLoaders)
            if (loader.id.startsWith("forge"))
                return loader.id.substring("forge-".length());
            
        return "Unknown";
    }
    
    /**
     * Class representation of the target minecraft instance properties.
     */
    public static class MinecraftData {
        
        /**
         * The target minecraft version.
         */
        public String version;
        
        /**
         * List of all modloaders expected.
         */
        public List<Modloader> modLoaders;
        
        /**
         * Class representation of a curse modloader entry.
         */
        public static class Modloader {
            
            /**
             * The ID for the mod loader.
             */
            public String id;
            
            /**
             * Whether or not the mod loader is a primary one.
             */
            public boolean primary;
            
        }
    }
    
    /**
     * Class representation of a curse file entry.
     */
    public static class FileData {
        
        /**
         * ID for a project.
         */
        public int projectID;
        
        /**
         * The file ID.
         */
        public int fileID;
        
        /**
         * Whether or not the file is essential.
         */
        public boolean required;
        
        @Override
        public String toString () {
            
            return this.projectID + "/" + this.fileID;
        }
    }
}