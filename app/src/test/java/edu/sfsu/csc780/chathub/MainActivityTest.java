package edu.sfsu.csc780.chathub;

import android.app.Notification;
import android.app.NotificationManager;
import android.os.Build;

import static org.mockito.Mockito.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import edu.sfsu.csc780.chathub.ui.MainActivity;


import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;



@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)

public class MainActivityTest {

    @Mock
    private MainActivity activity;
    @Mock
    private final Notification.Builder builder = new Notification.Builder(RuntimeEnvironment.application);


    @Before
    public void setup()
    {
        activity = Mockito.mock(MainActivity.class);
    }


    // @Test => JUnit 4 annotation specifying this is a test to be run
    // The test simply checks that our TextView exists and has the text "Hello world!"

    @Test
    public void build_setsContentTitleOnNotification() throws Exception {
        Notification notification = builder.setContentTitle("chatHubMessage").build();
        assertThat(shadowOf(notification).getContentTitle().toString()).isEqualTo("chatHubMessage");
    }


}
