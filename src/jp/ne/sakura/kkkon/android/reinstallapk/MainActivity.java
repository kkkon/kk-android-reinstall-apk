package jp.ne.sakura.kkkon.android.reinstallapk;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import jp.ne.sakura.kkkon.android.exceptionhandler.SettingsCompat;

public class MainActivity extends Activity
{
    private static final String TAG = "kk-ReInstall-Apk";

    private List<MyListData>    mDataList = new ArrayList<MyListData>(128);
    private ListView            mListView;
    private TextView            mUnknownSourceTextView;

    public class MyListData
    {
        private Drawable image;
        private String text;
        private String packageName;
        private long    firstInstallTime;
        private long    lastUpdateTime;

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

        public long getLastUpdateTime() {
            return lastUpdateTime;
        }
        public void setLastUpdateTime(long lastUpdateTime)
        {
            this.lastUpdateTime = lastUpdateTime;
        }

        
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d( TAG, "onCreate:");
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout( this );
        layout.setOrientation( LinearLayout.VERTICAL );

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
        layout.addView( mUnknownSourceTextView );
        ImageView imageView = new ImageView( this );
        layout.addView( imageView);

        mListView = new ListView( this );

        TextView emptyTextView = new TextView( this );
        emptyTextView.setText( "No items found" );
        mListView.setEmptyView( emptyTextView );

        {
            PackageManager  pm = getPackageManager();
            if ( null != pm )
            {
                final List<ApplicationInfo> listApplicationInfo = pm.getInstalledApplications( 0 );
                if ( null != listApplicationInfo )
                {
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
                            }

                            Log.d( TAG, "package=" + appInfo.packageName );
                            Log.d( TAG, "name=" + appInfo.name );
                            Log.d( TAG, "sourcedir=" + appInfo.sourceDir );
                            Log.d( TAG, "label=" + appInfo.loadLabel( pm ) );

                            MyListData item = new MyListData();
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
                                        final long lastUpdateTime = packageInfo.firstInstallTime; // API9

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
            }
        }
        MyAdapter adapter = new MyAdapter( this );
        mListView.setAdapter( adapter );

        layout.addView( mListView );

        setContentView( layout );
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
}
