package com.navibees.sdk.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.navibees.sdk.ApplicationConstants;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.navibees.sdk.util.CommonUtils.isFirstTimeInCurrentVersion;

/**
 * Created by nabilnoaman on 4/23/15.
 *
 * http://developer.android.com/training/basics/data-storage/files.html
 * http://stackoverflow.com/questions/21077172/how-to-copy-assets-sub-folders-files-on-sd-card-in-android
 * https://gist.github.com/tylerchesley/6198074
 * http://stackoverflow.com/questions/4447477/android-how-to-copy-files-from-assets-folder-to-sdcard
 */
public class AssetsManager {


    static final String TAG = "AssetsManager";

    private Context context;
    private AssetManagerListener assetManagerListener;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    public AssetsManager(Context context , AssetManagerListener assetManagerListener){
        this.context = context ;
        this.assetManagerListener = assetManagerListener;

        sp = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sp.edit();

    }

    public Boolean copyFileOrDir(String path) {
        AssetManager assetManager = this.context.getAssets();
        String fullPath = context.getFilesDir().getAbsolutePath()+ "/" + path ;//Internal Memory
        //Log.i(TAG, "-------copyFileOrDir fullPath :" + fullPath);
        String assets[] = null;
        try {
            assets = assetManager.list(path);
            if (assets.length == 0) {
                //Before Copy Check if File Exist Before in internal Memory (target location)
                //If it was exist so skip it
                File file = new File(fullPath);
                if(!file.exists()) {
                    copyFile(path);
                }
            } else {

                File dir = new File(fullPath);
                if (!dir.exists()) {
                    dir.mkdir();
                }

                for (int i = 0; i < assets.length; ++i) {
                    copyFileOrDir(path + "/" + assets[i]);
                }

          }

            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void copyFile(String filename) throws IOException {
        AssetManager assetManager = this.context.getAssets();

        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            String newFileName = context.getFilesDir().getAbsolutePath()+ "/" + filename ;//Internal Memory

            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            throw new IOException(e);
        }

    }


    private class ManageAssetFolders extends AsyncTask<Void, Void, Boolean>
    {

        @Override
        protected Boolean doInBackground(Void... arg0)
        {
            //Check Stored MAP_RESOURCES_APP_VERSION if Current Version is New then copy
            if(isFirstTimeInCurrentVersion(context)) {
                //New Version -- Remove Old then Copy
                String mapResourcesPath = context.getFilesDir().getAbsolutePath()+ "/" + ApplicationConstants.MAP_RESOURCES_FOLDER;
                deleteFolder(mapResourcesPath);
                return copyFileOrDir(ApplicationConstants.MAP_RESOURCES_FOLDER);
            }else {
                //Old Version
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if(success) {
                new ExtractMapResourcesFoldersTask().execute();
            }else{
                initAppConstants();
                assetManagerListener.assetsCopyCallback(true);
            }
        }
    }

    private void deleteFolder(String fullPath) {
        //Log.i(TAG, "-------deleteFolder fullPath :" + fullPath);
        File fileOrDir = new File(fullPath);
        String[] children = fileOrDir.list();
        if(children != null) {
            if (children.length == 0) {
                fileOrDir.delete();
            } else {
                //Directory
                for (int i = 0; i < children.length; ++i) {
                    deleteFolder(fullPath + "/" + children[i]);
                }
            }
        }

        if(fileOrDir.exists())
            fileOrDir.delete();
    }


    public void copyAssetsFolderInBackground(){
        new ManageAssetFolders().execute();
    }

    public interface AssetManagerListener {
        public void assetsCopyCallback(Boolean success);
    }



    private class ExtractMapResourcesFoldersTask extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(Void... arg0)
        {
            //Extract MapResources Folders
            boolean imagesExtracted = false;
            boolean metaDataExtracted = false;
            boolean networkDataSetsExtracted = false;
            boolean tileLayersExtracted = false;

            String imagesPath = context.getFilesDir().getAbsolutePath()+ "/" + ApplicationConstants.MAP_RESOURCES_FOLDER_IMAGE ;//Internal Memory
            String metaDataPath = context.getFilesDir().getAbsolutePath()+ "/" + ApplicationConstants.MAP_RESOURCES_FOLDER_META_DATA ;
            String networkDataSetsPath = context.getFilesDir().getAbsolutePath()+ "/" + ApplicationConstants.MAP_RESOURCES_FOLDER_NETWORK_DATASETS ;
            String tileLayersPath = context.getFilesDir().getAbsolutePath()+ "/" + ApplicationConstants.MAP_RESOURCES_FOLDER_TILED_LAYERS ;


            File imagesZipFile = new File(imagesPath+".zip");
            File metaDataZipFile = new File(metaDataPath+".zip");
            File networkDataSetsZipFile = new File(networkDataSetsPath+".zip");
            File tileLayersZipFile = new File(tileLayersPath+".zip");

            //Save Paths to Shared Preferences if its folder extracted
            imagesExtracted = extractZipFile(imagesZipFile , imagesPath);
            if (imagesExtracted) {
                ApplicationConstants.mapResourcesImagesPath = imagesPath + "/" + imagesZipFile.getName().substring(0, imagesZipFile.getName().length()-4);
                editor.putString(ApplicationConstants.MAP_RESOURCES_IMAGES_PATH_KEY, ApplicationConstants.mapResourcesImagesPath);
            }

            metaDataExtracted = extractZipFile( metaDataZipFile , metaDataPath);
            if (metaDataExtracted) {
                ApplicationConstants.mapResourcesMetadataPath = metaDataPath + "/" + metaDataZipFile.getName().substring(0, metaDataZipFile.getName().length()-4);
                editor.putString(ApplicationConstants.MAP_RESOURCES_META_DATA_PATH_KEY, ApplicationConstants.mapResourcesMetadataPath);
            }

            networkDataSetsExtracted = extractZipFile( networkDataSetsZipFile , networkDataSetsPath);
            if (networkDataSetsExtracted) {
                ApplicationConstants.mapResourcesNetworkDatasetsPath = networkDataSetsPath + "/" + networkDataSetsZipFile.getName().substring(0, networkDataSetsZipFile.getName().length()-4);
                editor.putString(ApplicationConstants.MAP_RESOURCES_NETWORK_DATASETS_PATH_KEY, ApplicationConstants.mapResourcesNetworkDatasetsPath);
            }


            tileLayersExtracted = extractZipFile( tileLayersZipFile , tileLayersPath);
            if (tileLayersExtracted) {
                ApplicationConstants.mapResourcesTiledLayerPath = tileLayersPath + "/" + tileLayersZipFile.getName().substring(0, tileLayersZipFile.getName().length()-4);
                editor.putString(ApplicationConstants.MAP_RESOURCES_TILED_LAYER_PATH_KEY , ApplicationConstants.mapResourcesTiledLayerPath);
            }

            //Save App Version Of This MapResources
            boolean allSuccess = imagesExtracted  && metaDataExtracted && networkDataSetsExtracted && tileLayersExtracted;
            if(allSuccess) {
                try {
                    int versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
                    editor.putInt(ApplicationConstants.MAP_RESOURCES_APP_VERSION_KEY, versionCode);
                    Log.d(TAG, "versionCode :" + versionCode);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, e.toString());
                }
            }

            editor.commit();

            return allSuccess;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            //Load Paths from Shared Preferences

            initAppConstants();
            Log.i(TAG, "------- ExtractMapResourcesFoldersTask ApplicationConstants.mapResourcesImagesPath :" + ApplicationConstants.mapResourcesImagesPath);
            Log.i(TAG, "------- ExtractMapResourcesFoldersTask ApplicationConstants.mapResourcesMetadataPath :" + ApplicationConstants.mapResourcesMetadataPath);
            Log.i(TAG, "------- ExtractMapResourcesFoldersTask ApplicationConstants.mapResourcesNetworkDatasetsPath :" + ApplicationConstants.mapResourcesNetworkDatasetsPath);
            Log.i(TAG, "------- ExtractMapResourcesFoldersTask ApplicationConstants.mapResourcesTiledLayerPath :" + ApplicationConstants.mapResourcesTiledLayerPath);

            assetManagerListener.assetsCopyCallback(success);
        }
    }

    private void initAppConstants(){
        ApplicationConstants.mapResourcesImagesPath = sp.getString(ApplicationConstants.MAP_RESOURCES_IMAGES_PATH_KEY, null);
        ApplicationConstants.mapResourcesMetadataPath = sp.getString(ApplicationConstants.MAP_RESOURCES_META_DATA_PATH_KEY, null);
        ApplicationConstants.mapResourcesNetworkDatasetsPath = sp.getString(ApplicationConstants.MAP_RESOURCES_NETWORK_DATASETS_PATH_KEY , null);
        ApplicationConstants.mapResourcesTiledLayerPath = sp.getString(ApplicationConstants.MAP_RESOURCES_TILED_LAYER_PATH_KEY , null);
    }

    /**
     * Unpacks the MapResources files.
     *
     * @param zipped_file    The file to be unpacked. has to be in zip format.
     * @param destination_dir Where to unpack the map resources file.
     */
    private boolean extractZipFile( File zipped_file,
                                       String destination_dir )
    {
        BufferedInputStream entry_stream = null;
        BufferedOutputStream dest_stream = null;
        try {
            ZipFile zip_file = new ZipFile( zipped_file );
            File dest_dir = new File( destination_dir );
            if ( dest_dir.isDirectory() ) {
                Log.d( TAG, "Destination directory already exists for the MapResources:folder:"+zipped_file.getName() + " , destination_dir = "+destination_dir);
                return false;
            }
            if ( !dest_dir.mkdirs() ) {
                Log.d( TAG, "Failed to create destination directory for the MapResources:folder:"+zipped_file.getName() );
                return false;
            }
            Enumeration zip_entries = zip_file.entries();
            //Log.d( TAG, "Unpacking the MapResources:folder:"+zipped_file.getName());
            while ( zip_entries.hasMoreElements() ) {
                ZipEntry entry = (ZipEntry) zip_entries.nextElement();
                //Log.d( TAG, String.format( "File: %s", entry.getName() ) );
                File dest_file = new File( destination_dir, entry.getName() );
                dest_file.getParentFile().mkdirs();
                if ( !entry.isDirectory() ) {
                    entry_stream = new BufferedInputStream(
                            zip_file.getInputStream( entry ) );

                    dest_stream = new BufferedOutputStream(
                            new FileOutputStream( dest_file ) );
                    byte buffer[] = new byte[1024];
                    int count;
                    while ( (count =
                            entry_stream.read( buffer, 0, 1024 )) != -1 ) {
                        dest_stream.write( buffer, 0, count );
                    }
                    dest_stream.flush();
                    dest_stream.close();
                    entry_stream.close();
                }
            }
            return true;
        } catch ( IOException e ) {
            e.printStackTrace();
            Log.d( TAG, "Failed to unpack the MapResources:folder:"+zipped_file.getName() );
        } finally {
            try {
                if ( entry_stream != null ) {
                    entry_stream.close();
                }
                if ( dest_stream != null ) {
                    dest_stream.close();
                }
            } catch ( IOException ex ) {

            }
        }
        return false;
    }
}
