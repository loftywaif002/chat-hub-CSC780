package edu.sfsu.csc780.chathub.ui;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.sfsu.csc780.chathub.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest2 {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void mainActivityTest2() {
        ViewInteraction rc = onView(
                allOf(withText("Sign in"),
                        withParent(allOf(withId(R.id.sign_in_button),
                                withParent(withId(R.id.sign_in_layout)))),
                        isDisplayed()));
        rc.perform(click());

        ViewInteraction appCompatImageView = onView(
                allOf(withId(R.id.emoji_btn), isDisplayed()));
        appCompatImageView.perform(click());

        ViewInteraction emojiconTextView = onView(
                allOf(withId(R.id.emojicon_icon), withText("��"),
                        withParent(childAtPosition(
                                withId(R.id.Emoji_GridView),
                                0)),
                        isDisplayed()));
        emojiconTextView.perform(click());

        ViewInteraction emojiconEditText = onView(
                allOf(withId(R.id.messageEditText),
                        withParent(allOf(withId(R.id.linearLayout),
                                withParent(withId(R.id.root_view)))),
                        isDisplayed()));
        emojiconEditText.perform(replaceText("��"), closeSoftKeyboard());

        ViewInteraction emojiconTextView2 = onView(
                allOf(withId(R.id.emojicon_icon), withText("��"),
                        withParent(childAtPosition(
                                withId(R.id.Emoji_GridView),
                                1)),
                        isDisplayed()));
        emojiconTextView2.perform(click());

        ViewInteraction emojiconEditText2 = onView(
                allOf(withId(R.id.messageEditText), withText("��"),
                        withParent(allOf(withId(R.id.linearLayout),
                                withParent(withId(R.id.root_view)))),
                        isDisplayed()));
        emojiconEditText2.perform(replaceText("����"), closeSoftKeyboard());

        ViewInteraction emojiconTextView3 = onView(
                allOf(withId(R.id.emojicon_icon), withText("��"),
                        withParent(childAtPosition(
                                withId(R.id.Emoji_GridView),
                                2)),
                        isDisplayed()));
        emojiconTextView3.perform(click());

        ViewInteraction emojiconEditText3 = onView(
                allOf(withId(R.id.messageEditText), withText("����"),
                        withParent(allOf(withId(R.id.linearLayout),
                                withParent(withId(R.id.root_view)))),
                        isDisplayed()));
        emojiconEditText3.perform(replaceText("������"), closeSoftKeyboard());

        ViewInteraction emojiconTextView4 = onView(
                allOf(withId(R.id.emojicon_icon), withText("��"),
                        withParent(childAtPosition(
                                withId(R.id.Emoji_GridView),
                                3)),
                        isDisplayed()));
        emojiconTextView4.perform(click());

        ViewInteraction emojiconEditText4 = onView(
                allOf(withId(R.id.messageEditText), withText("������"),
                        withParent(allOf(withId(R.id.linearLayout),
                                withParent(withId(R.id.root_view)))),
                        isDisplayed()));
        emojiconEditText4.perform(replaceText("��������"), closeSoftKeyboard());

        ViewInteraction emojiconTextView5 = onView(
                allOf(withId(R.id.emojicon_icon), withText("☺"),
                        withParent(childAtPosition(
                                withId(R.id.Emoji_GridView),
                                4)),
                        isDisplayed()));
        emojiconTextView5.perform(click());

        ViewInteraction emojiconEditText5 = onView(
                allOf(withId(R.id.messageEditText), withText("��������"),
                        withParent(allOf(withId(R.id.linearLayout),
                                withParent(withId(R.id.root_view)))),
                        isDisplayed()));
        emojiconEditText5.perform(replaceText("��������☺"), closeSoftKeyboard());

        ViewInteraction emojiconTextView6 = onView(
                allOf(withId(R.id.emojicon_icon), withText("��"),
                        withParent(childAtPosition(
                                withId(R.id.Emoji_GridView),
                                5)),
                        isDisplayed()));
        emojiconTextView6.perform(click());

        ViewInteraction emojiconEditText6 = onView(
                allOf(withId(R.id.messageEditText), withText("��������☺"),
                        withParent(allOf(withId(R.id.linearLayout),
                                withParent(withId(R.id.root_view)))),
                        isDisplayed()));
        emojiconEditText6.perform(replaceText("��������☺��"), closeSoftKeyboard());

        ViewInteraction floatingActionButton = onView(
                allOf(withId(R.id.sendButton), isDisplayed()));
        floatingActionButton.perform(click());

        ViewInteraction appCompatImageButton = onView(
                allOf(withId(R.id.recordBtn), isDisplayed()));
        appCompatImageButton.perform(click());

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.messageRecyclerView),
                        withParent(allOf(withId(R.id.root_view),
                                withParent(withId(android.R.id.content)))),
                        isDisplayed()));
        recyclerView.perform(actionOnItemAtPosition(4, click()));

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
