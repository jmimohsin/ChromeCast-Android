package com.example.admin.chromeexample;

import java.io.IOException;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.cast.RemoteMediaPlayer.MediaChannelResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/*
 * Created by Mohsin
 */

public class MainActivity extends ActionBarActivity {

    private MediaRouter mediaRouter;
    private MediaRouteSelector mediaRouteSelector;
    private CastDevice selectedDevice;
    private GoogleApiClient apiClient;
    private boolean applicationStarted;
    private String APP_ID=CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID;
    private String MSG_NAMESPACE="urn:x-cast:appypie.chromecast.message";
    private String VIDEO_NAMESPACE="urn:x-cast:appypie.chromecast.video";
    private RemoteMediaPlayer mRemoteMediaPlayer;
    private Button bt_playVideo;
    private boolean flag_play=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bt_playVideo=(Button)findViewById(R.id.button2);

        mediaRouter = MediaRouter.getInstance(getApplicationContext());
        mediaRouteSelector = new MediaRouteSelector.Builder()
         .addControlCategory(CastMediaControlIntent.categoryForCast(APP_ID)).build();

        //for media
        mRemoteMediaPlayer = new RemoteMediaPlayer();
        mRemoteMediaPlayer.setOnStatusUpdatedListener(new RemoteMediaPlayer.OnStatusUpdatedListener() {
            @Override
            public void onStatusUpdated() {
                	MediaStatus mediaStatus = mRemoteMediaPlayer
                			.getMediaStatus();
                	boolean isPlaying = mediaStatus.getPlayerState() == MediaStatus.PLAYER_STATE_PLAYING;
                    if(!isPlaying){
                        flag_play=false;
                        bt_playVideo.setText("PLAY VIDEO");
                    }

            }
        });

        mRemoteMediaPlayer.setOnMetadataUpdatedListener(new RemoteMediaPlayer.OnMetadataUpdatedListener() {
            @Override
            public void onMetadataUpdated() {
                //MediaInfo mediaInfo = mRemoteMediaPlayer.getMediaInfo();
                //MediaMetadata metadata = mediaInfo.getMetadata();
            }
        });

    }

    //buttons click method
    public void action(View v){
        int id=v.getId();
        if(id==R.id.button1){
            //send message
            sendMessage("Have a good day.");
        }
        else if(id==R.id.button2){
            //play video
            System.out.println("play");
            playVideo("http://snappy.appypie.com/media/user_space/1875201f2941/2016shesup.mp4");
        }
    }

    //Send message
    private void sendMessage(String message)
    {
        if (apiClient != null)
        {
            try
            {
                Cast.CastApi.sendMessage(apiClient, MSG_NAMESPACE, message)
                        .setResultCallback(new ResultCallback<Status>()
                        {
                            @Override
                            public void onResult(Status result)
                            {
                                if (!result.isSuccess())
                                {
                                    Toast.makeText(getApplicationContext(), "Sending message failed.", Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(getApplicationContext(), "Message sent.", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
            catch (Exception e)
            {
            }
        }
    }

    //Play video
    private void playVideo(String video) {
        if(apiClient==null)
            return;

        try {
            if(flag_play) {
                mRemoteMediaPlayer.pause(apiClient);
                bt_playVideo.setText("PLAY VIDEO");
                flag_play=false;

            } else {
                bt_playVideo.setText("PAUSE VIDEO");
                flag_play=true;
                MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
                mediaMetadata.putString(MediaMetadata.KEY_TITLE, "My video");

                MediaInfo mediaInfo = new MediaInfo.Builder(video)
                        .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                        .setMetadata(mediaMetadata)
                        .setContentType("video/mp4")
                        .build();

                mRemoteMediaPlayer.load(apiClient, mediaInfo, true)
                        .setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                            @Override
                            public void onResult(MediaChannelResult result) {
                                if (result.getStatus().isSuccess()) {
                                }else{
                                    Toast.makeText(getApplicationContext(), "Failed to load Video.", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider)MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        mediaRouteActionProvider.setRouteSelector(mediaRouteSelector);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
        return super.onOptionsItemSelected(item);
    }

    //To search and select device
    private final MediaRouter.Callback mediaRouterCallback = new MediaRouter.Callback()
    {
        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info)
        {
            selectedDevice = CastDevice.getFromBundle(info.getExtras());
            setSelectedDevice(selectedDevice);
            // String routeId = info.getId();
            System.out.println("onRouteSelected");
            Toast.makeText(getApplicationContext(), "Selected device:"+selectedDevice, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info)
        {
            selectedDevice=null;
            setSelectedDevice(selectedDevice);
            System.out.println("onRouteUnselected");
            Toast.makeText(getApplicationContext(), "No device selected.", Toast.LENGTH_LONG).show();
        }

    };

    //Set selected device
    private void setSelectedDevice(CastDevice device)
    {
        selectedDevice = device;
        System.out.println("setSelectedDevice: "+selectedDevice);
        if (selectedDevice != null)
        {
            try
            {
                stopApplication();
                disconnectApiClient();
                connectApiClient();
            }
            catch (IllegalStateException e)
            {
                disconnectApiClient();
            }
        }
        else
        {
            if (apiClient != null)
            {
                disconnectApiClient();
            }

            mediaRouter.selectRoute(mediaRouter.getDefaultRoute());
        }
    }

    //disconnect GoogleApiClient
    private void disconnectApiClient()
    {
        if (apiClient != null)
        {
            apiClient.disconnect();
            apiClient = null;
        }
    }

    //Stop GoogleApiClient
    private void stopApplication()
    {
        if (apiClient == null)
            return;

        if (applicationStarted)
        {
            Cast.CastApi.stopApplication(apiClient);
            applicationStarted = false;
        }
    }

    //Connect GoogleApiClient
    private void connectApiClient()
    {
        Cast.CastOptions apiOptions = Cast.CastOptions.builder(selectedDevice, castClientListener).build();
        apiClient = new GoogleApiClient.Builder(this)
                .addApi(Cast.API, apiOptions)
                .addConnectionCallbacks(connectionCallback)
                .addOnConnectionFailedListener(connectionFailedListener)
                .build();
        apiClient.connect();
        System.out.println("connectApiClient");
        Toast.makeText(getApplicationContext(), "ApiClient Connected.", Toast.LENGTH_LONG).show();
    }

    //Listeners related to connectApiClient() method
    private final Cast.Listener castClientListener = new Cast.Listener()
    {
        @Override
        public void onApplicationDisconnected(int statusCode)
        {
            try {
                Cast.CastApi.removeMessageReceivedCallbacks(apiClient, MSG_NAMESPACE);
                Cast.CastApi.removeMessageReceivedCallbacks(apiClient, VIDEO_NAMESPACE);
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void onVolumeChanged()
        {
            //if (apiClient != null)
            //Cast.CastApi.getVolume(mApiClient);
        }
        @Override
        public void onApplicationStatusChanged() {
            //if (apiClient != null)
            //	Cast.CastApi.getApplicationStatus(apiClient);

        }
    };

    private final GoogleApiClient.ConnectionCallbacks connectionCallback = new GoogleApiClient.ConnectionCallbacks()
    {
        @Override
        public void onConnected(Bundle bundle)
        {
            try
            {
                Cast.CastApi.launchApplication(apiClient, APP_ID, false).setResultCallback(connectionResultCallback);
            }
            catch (Exception e)
            {

            }
        }

        @Override
        public void onConnectionSuspended(int i)
        {
        }
    };

    private final GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener()
    {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult)
        {
            setSelectedDevice(null);
            System.out.println("connectionFailed.");
        }
    };

    private final ResultCallback<Cast.ApplicationConnectionResult> connectionResultCallback = new ResultCallback<Cast.ApplicationConnectionResult>()
    {
        @Override
        public void onResult(Cast.ApplicationConnectionResult result)
        {
            Status status = result.getStatus();
            if (status.isSuccess())
            {
                applicationStarted = true;
                try {
                    //creating custom channel to send message
                    Cast.CastApi.setMessageReceivedCallbacks(apiClient, MSG_NAMESPACE, incomingMsgHandler);
                    //creating custom channel to play video
                    Cast.CastApi.setMessageReceivedCallbacks(apiClient, VIDEO_NAMESPACE, mRemoteMediaPlayer);

                    mRemoteMediaPlayer.requestStatus(apiClient)
                            .setResultCallback(
                                    new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                                        @Override
                                        public void onResult(MediaChannelResult result) {
                                            if (!result.getStatus().isSuccess()) {
                                                //Log.e(TAG, "Failed to request status.");
                                                Toast.makeText(getApplicationContext(), "Failed to request status.", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    };

    public final Cast.MessageReceivedCallback incomingMsgHandler = new Cast.MessageReceivedCallback()
    {
        @Override
        public void onMessageReceived(CastDevice castDevice, String namespace, String message)
        {
        }
    };


    @Override
    protected void onStart()
    {
        super.onStart();
        mediaRouter.addCallback(mediaRouteSelector, mediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
    }

    @Override
    protected void onStop()
    {
        //setSelectedDevice(null);
        mediaRouter.removeCallback(mediaRouterCallback);
        super.onStop();
    }
}
