package jp.ne.sakura.kkkon.android.reinstallapk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import jp.ne.sakura.kkkon.android.exceptionhandler.SettingsCompat;

public class MainActivity extends Activity implements ListView.OnItemClickListener
{
    private static final String TAG = "kk-ReInstall-Apk";

    private List<MyListData>    mDataList = new ArrayList<MyListData>(128);
    private ListView            mListView;
    private TextView            mUnknownSourceTextView;
    private LinearLayout        mLayout;

    private static final int    INTENT_REQUEST_CODE_INSTALL = 0;
    private List<Integer>       mUpdateList = new ArrayList<Integer>(16);

    public class MyListData
    {
        private Drawable image;
        private String text;
        private String packageName;
        private long    firstInstallTime;
        private long    lastUpdateTime;
        private String  apkPath;

        public void setImage( final Drawable image )
        {
            this.image = image;
        }
        public Drawable getImage()
        {
            return this.image;
        }

        public void setPackageName( final String packageName )
        {
            this.packageName = packageName;
        }
        public String getPackageName()
        {
            return this.packageName;
        }

        public void setText( final String text )
        {
            this.text = text;
        }
        public String getText()
        {
            return this.text;
        }

        public long getFirstInstallTime()
        {
            return firstInstallTime;
        }
        public void setFirstInstallTime(long firstInstallTime)
        {
            this.firstInstallTime = firstInstallTime;
        }

        public long getLastUpdateTime()
        {
            return lastUpdateTime;
        }
        public void setLastUpdateTime(long lastUpdateTime)
        {
            this.lastUpdateTime = lastUpdateTime;
        }

        public long getLatestTime()
        {
            final long latestTime = Math.max( lastUpdateTime, firstInstallTime );
            return latestTime;
        }

        public String getApkPath()
        {
            return apkPath;
        }
        public void setApkPath(String apkPath)
        {
            this.apkPath = apkPath;
        }

    }

    public class ComparatorLastestTime implements Comparator<MyListData>
    {
        public int compare(MyListData o1, MyListData o2)
        {
            int result = 0;

            if ( null == o1 )
            {
                return result;
            }
            if ( null == o2 )
            {
                return result;
            }

            {
                final long value = o2.getLatestTime() - o1.getLatestTime();
                if ( value < 0 )
                {
                    result = -1;
                }
                else
                if ( value > 0 )
                {
                    result = 1;
                }
            }

            return result;
        }

    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d( TAG, "onCreate:");
        super.onCreate(savedInstanceState);

        mLayout = new LinearLayout( this );
        mLayout.setOrientation( LinearLayout.VERTICAL );

        mUnknownSourceTextView = new TextView( this );
        {
            final StringBuilder sb = new StringBuilder();

            {
                final String label = this.getString( R.string.system_setting );
                if ( null != label )
                {
                    sb.append( label );
                }
            }
            sb.append( ": " );
            {
                final String label = this.getString( R.string.unknown_sources );
                if ( null != label )
                {
                    sb.append( label );
                }
            }
            sb.append( " " );

            SettingsCompat.initialize( this.getApplicationContext() );
            final boolean isAllow = SettingsCompat.isAllowedNonMarketApps();
            if ( isAllow )
            {
                final String label = this.getString( R.string.unknown_sources_ok );
                if ( null != label )
                {
                    sb.append( label );
                }
                else
                {
                    sb.append( "OK" );
                }
            }
            else
            {
                final String label = this.getString( R.string.unknown_sources_ng );
                if ( null != label )
                {
                    sb.append( label );
                }
                else
                {
                    sb.append( "NG" );
                }
            }
            mUnknownSourceTextView.setGravity( Gravity.RIGHT );
            mUnknownSourceTextView.setText( sb.toString() );
        }
        mLayout.addView( mUnknownSourceTextView );
        ImageView imageView = new ImageView( this );
        mLayout.addView( imageView);

        mListView = new ListView( this );

        TextView emptyTextView = new TextView( this );
        emptyTextView.setText( "No items found" );
        mListView.setEmptyView( emptyTextView );

        makeApplicationList();
        Collections.sort( mDataList, new ComparatorLastestTime() );

        MyAdapter adapter = new MyAdapter( this );
        mListView.setAdapter( adapter );

        mListView.setOnItemClickListener( this );

        mLayout.addView( mListView );

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( this );
        alertDialogBuilder.setMessage( R.string.warning_text );
        alertDialogBuilder.setNegativeButton( R.string.warning_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface di, int i)
            {
                System.exit( 0 );
            }
        });
        alertDialogBuilder.setPositiveButton( R.string.warning_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface di, int i)
            {
                setContentView( mLayout );
            }
        });
        alertDialogBuilder.setOnCancelListener( new DialogInterface.OnCancelListener()
        {
            public void onCancel(DialogInterface di) {
                System.exit( 0 );
            }
        });

        alertDialogBuilder.show();
        //setContentView( mLayout );
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    private void makeApplicationList()
    {
        PackageManager  pm = getPackageManager();
        if ( null == pm )
        {
            return;
        }

        final List<ApplicationInfo> listApplicationInfo = pm.getInstalledApplications( 0 );
        if ( null == listApplicationInfo )
        {
            return;
        }

        for ( final ApplicationInfo appInfo : listApplicationInfo )
        {
            if ( null == appInfo )
            {
                continue;
            }
            if ( null != appInfo.sourceDir )
            {
                if ( appInfo.sourceDir.startsWith( "/system/" ) )
                {
                    continue;
                }
                if ( null != appInfo.packageName )
                {
                    if ( appInfo.packageName.startsWith( "com.example." ) )
                    {
                        continue;
                    }
                    if ( appInfo.packageName.startsWith( "com.android." ) )
                    {
                        continue;
                    }

                    if ( appInfo.packageName.startsWith( "com.google.android." ) )
                    {
                        continue;
                    }
                }

                Log.d( TAG, "package=" + appInfo.packageName );
                Log.d( TAG, "name=" + appInfo.name );
                Log.d( TAG, "sourcedir=" + appInfo.sourceDir );
                Log.d( TAG, "label=" + appInfo.loadLabel( pm ) );

                MyListData item = new MyListData();
                item.setApkPath( appInfo.sourceDir );
                {
                    final CharSequence label = appInfo.loadLabel( pm );
                    if ( null == label )
                    {
                        item.setText( appInfo.packageName );
                    }
                    else
                    {
                        item.setText( label.toString() );
                    }
                }
                item.setPackageName( appInfo.packageName );

                final Drawable drawable = appInfo.loadIcon(pm);
                if ( null != drawable )
                {
                    Log.d( TAG, "icon: w=" + drawable.getIntrinsicWidth() + ",h=" + drawable.getIntrinsicHeight() );
                }
                item.setImage( drawable );

                {
                    try
                    {
                        final PackageInfo packageInfo = pm.getPackageInfo( appInfo.packageName, 0 );
                        if ( null != packageInfo )
                        {
                            final long firstInstallTime = packageInfo.firstInstallTime; // API9
                            final long lastUpdateTime = packageInfo.lastUpdateTime; // API9

                            Log.d( TAG,  "firstInstallTime=" + firstInstallTime );
                            Log.d( TAG,  "lastUpdateTime=" + lastUpdateTime );
                            item.setFirstInstallTime( firstInstallTime );
                            item.setLastUpdateTime( lastUpdateTime );
                        }
                    }
                    catch ( PackageManager.NameNotFoundException e )
                    {
                        Log.e( TAG, "got Exception=" + e.toString(), e );
                    }
                }


                Log.d( TAG,  "" );
                mDataList.add( item );
            }
        }

    }





    public class MyAdapter extends BaseAdapter
    {
        private Context mContext;

        public MyAdapter( Context context )
        {
            this.mContext = context;
        }

        @Override
        public int getCount()
        {
            if ( null != mDataList )
            {
                return mDataList.size();
            }

            return 0;
        }

        @Override
        public Object getItem(int i)
        {
            if ( null != mDataList )
            {
                return mDataList.get(i);
            }

            return null;
        }

        @Override
        public long getItemId(int i)
        {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup vg)
        {
            View v = view;
            if ( null == v )
            {

                LinearLayout layout = new LinearLayout( mContext );
                layout.setOrientation( LinearLayout.HORIZONTAL );

                ImageView imageView = new ImageView( mContext );
                imageView.setId( 0 );
                imageView.setScaleType( ImageView.ScaleType.FIT_XY );
                imageView.setLayoutParams( new ViewGroup.LayoutParams( 144, 144 ) );
                imageView.setAdjustViewBounds( true );

                TextView textView = new TextView( mContext );
                textView.setId( 1 );
                textView.setLayoutParams( new ViewGroup.LayoutParams( ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT ) );
                textView.setGravity( Gravity.CENTER_VERTICAL );
                textView.setPadding( 20, 20, 20, 20 );

                layout.addView( imageView );
                layout.addView( textView );

                v = layout;
            }

            MyListData itemData = (MyListData)this.getItem(i);
            if ( null != itemData )
            {
                ImageView imageView = (ImageView) v.findViewById(0);
                TextView textView = (TextView) v.findViewById(1);
                if ( null != imageView )
                {
                    imageView.setBackgroundDrawable( itemData.getImage() );
                }
                if ( null != textView )
                {
                    textView.setText( itemData.getText() );
                }
            }

            return v;
        }

    }

    public void onItemClick(AdapterView<?> av, View view, int position, long id)
    {
        if ( this.mListView == av )
        {
            if ( null == this.mDataList )
            {
                Log.d( TAG, "mDataList is null " );
                return;
            }

            final MyListData itemData = this.mDataList.get( position );
            if ( null == itemData )
            {
                Log.d( TAG, "itemData is null index=" + position );
                return;
            }

            {
                final String apkPath = itemData.getApkPath();

                boolean installCalled = false;
                if ( null != apkPath )
                {
                    final File fileApk = new File(apkPath);
                    if ( fileApk.exists() )
                    {
                        installCalled = true;
                        mUpdateList.add( position );

                        Intent promptInstall = new Intent(Intent.ACTION_VIEW);
                        promptInstall.setDataAndType(Uri.fromFile( fileApk ), "application/vnd.android.package-archive");
                        //promptInstall.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                        this.startActivityForResult( promptInstall, INTENT_REQUEST_CODE_INSTALL );
                    }
                    else
                    {
                        Log.d( TAG, "fileApk not exists. path=" + fileApk.getAbsolutePath() );
                    }
                }
                else
                {
                    Log.d( TAG, "apkPath is null" );
                }

                if ( !installCalled )
                {
                    Toast   toast = Toast.makeText( this, R.string.apk_not_found, Toast.LENGTH_LONG );
                    toast.show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d( TAG, "requestCode=" + requestCode + ",resultCode=" + resultCode + ",intent=" + data );

        if ( INTENT_REQUEST_CODE_INSTALL == requestCode )
        {
            boolean needRefresh = false;

            if ( null != mUpdateList )
            {
                if ( ! mUpdateList.isEmpty() )
                {
                    Log.d( TAG, "mUpdateList: size=" + mUpdateList.size() );
                    final PackageManager pm = this.getPackageManager();
                    if ( null != pm )
                    {
                        for ( final Integer itemPosition : mUpdateList )
                        {
                            if ( null == itemPosition )
                            {
                                continue;
                            }

                            Log.d( TAG, "mUpdateList: itemPosition=" + itemPosition );
                            final MyListData itemData = mDataList.get( itemPosition );
                            if ( null != itemData )
                            {
                                try
                                {
                                    final String apkPath = itemData.getApkPath();
                                    final ApplicationInfo appInfo = pm.getApplicationInfo( itemData.packageName, 0 );
                                    if ( null != appInfo )
                                    {
                                        if ( null != appInfo.sourceDir && null != apkPath )
                                        {
                                            if ( 0 != appInfo.sourceDir.compareTo( apkPath) )
                                            {
                                                Log.d( TAG, "old ApkPath=" + apkPath );
                                                Log.d( TAG, "new ApkPath=" + appInfo.sourceDir );

                                                itemData.setApkPath( appInfo.sourceDir );
                                                {
                                                    try
                                                    {
                                                        final PackageInfo packageInfo = pm.getPackageInfo( appInfo.packageName, 0 );
                                                        if ( null != packageInfo )
                                                        {
                                                            final long firstInstallTime = packageInfo.firstInstallTime; // API9
                                                            final long lastUpdateTime = packageInfo.lastUpdateTime; // API9

                                                            Log.d( TAG,  "firstInstallTime=" + firstInstallTime );
                                                            Log.d( TAG,  "lastUpdateTime=" + lastUpdateTime );
                                                            itemData.setFirstInstallTime( firstInstallTime );
                                                            itemData.setLastUpdateTime( lastUpdateTime );
                                                        }
                                                    }
                                                    catch ( PackageManager.NameNotFoundException e )
                                                    {
                                                        Log.e( TAG, "got Exception=" + e.toString(), e );
                                                    }
                                                }

                                                mDataList.set( itemPosition, itemData );

                                                needRefresh = true;
                                            }
                                        }
                                    }
                                }
                                catch ( PackageManager.NameNotFoundException e )
                                {
                                    Log.d( TAG, "got Exception: " + e.toString(), e );
                                }
                            }
                        }
                    }
                }

                mUpdateList.clear();
                Collections.sort( mDataList, new ComparatorLastestTime() );
            }

            if ( needRefresh )
            {
                if ( null != mListView )
                {
                    //mListView.requestLayout();
                    mListView.invalidateViews();
                }
            }
        }
    }


}
