package jp.ne.sakura.kkkon.android.reinstallapk;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by kkkon on 2018/08/05.
 */

public class FileProvider
    extends ContentProvider
{
    private static final String TAG = "kk-ReInstall-FileProv";

    private static final String MY_AUTHORITY = "jp.ne.sakura.kkkon.android.reinstallapk.provider";
    private static final String MY_MIME_TYPE = "application/vnd.android.package-archive";

    @Override
    public boolean onCreate()
    {
        Log.d(TAG, "#onCreate" );
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1)
    {
        Log.d(TAG, "#query uri=" + uri );
        return null;
    }

    @Override
    public String getType(Uri uri)
    {
        Log.d(TAG, "#getType uri=" + uri );

        if ( true )
        {
            return MY_MIME_TYPE;
        }
        else
        {
            final File file = null;
            final String fileName = file.getName();

            final int indexLastDot = fileName.lastIndexOf('.');
            if ( 0 <= indexLastDot )
            {
                final String extension = fileName.substring(indexLastDot+1);
                final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                if ( null != mime )
                {
                    return mime;
                }
            }

            return "application/octet-stream";
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues)
    {
        throw new UnsupportedOperationException("No insert for ro");
        //return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings)
    {
        throw new UnsupportedOperationException("No delete for ro");
        //return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings)
    {
        throw new UnsupportedOperationException("No updates for ro");
        //return 0;
    }

    @Override
    public void attachInfo(Context context, ProviderInfo info)
    {
        super.attachInfo(context, info);
    }


    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) // API19
            throws FileNotFoundException
    {
        Log.d(TAG, String.format("#openFile uri=%s, mode=%s", uri, mode) );

        final String pathEncoded = uri.getEncodedPath();
        //final int index = pathEncoded.indexOf( '/', 1 );
        //final String path = Uri.decode( pathEncoded.substring( index + 1 ) );
        final String path = Uri.decode( pathEncoded );
        Log.d(TAG, String.format("#openFile path=%s", path) );
        final File result = new File( path );
        if ( !result.exists() )
        {
            throw new FileNotFoundException(result.getAbsolutePath());
        }
        return ParcelFileDescriptor.open( result, ParcelFileDescriptor.MODE_READ_ONLY );
    }

    public static Uri getUriForFile(final File file )
    {
        final String path = Uri.encode(file.getAbsolutePath(), "/");
        final String authority = MY_AUTHORITY;
        final Uri result = new Uri.Builder()
                .scheme("content")
                .authority(authority)
                .encodedPath(path)
                .build();
        Log.d(TAG, String.format("#getUriForFile uri=%s", result) );
        return result;
    }
}
