package com.jayway.android.robotium.solo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.Assert;
import android.app.Activity;
import android.app.Instrumentation;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.TextView;

/**
 * This class contains various click methods. Examples are: clickOnButton(),
 * clickOnText(), clickOnScreen().
 * 
 * @author Renas Reda, renas.reda@jayway.com
 * 
 */

class Clicker {
	
	private final String LOG_TAG = "Robotium";
	private final ActivityFetcher soloActivity;
	private final ViewFetcher soloView;
	private final Scroller soloScroll;
	private final Instrumentation inst;


	/**
	 * Constructs this object.
	 * 
	 * @param soloActivity the {@link Activity} instance.
	 * @param soloView the {@link ViewFetcher} instance.
	 * @param soloScroll the {@link Scroller} instance.
	 * @param inst the {@link Instrumentation} instance.
	 */

	public Clicker(ActivityFetcher soloActivity, ViewFetcher soloView,
			Scroller soloScroll, Instrumentation inst) {

		this.soloActivity = soloActivity;
		this.soloView = soloView;
		this.soloScroll = soloScroll;
		this.inst = inst;
	}
	
	/**
	 * Private method to click on a specific coordinate on the screen
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 *
	 */
	
	private void clickOnScreen(float x, float y) {
		long downTime = SystemClock.uptimeMillis();
		long eventTime = SystemClock.uptimeMillis();
		MotionEvent event = MotionEvent.obtain(downTime, eventTime,
				MotionEvent.ACTION_DOWN, x, y, 0);
		MotionEvent event2 = MotionEvent.obtain(downTime, eventTime,
				MotionEvent.ACTION_UP, x, y, 0);
		try{
		inst.sendPointerSync(event);
		inst.sendPointerSync(event2);
		}catch(Throwable e){}
	}
	
	/**
	 * Private method to long click on a specific coordinate on the screen
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 *
	 */
	
	private void clickLongOnScreen(float x, float y) {
		long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0);
        inst.sendPointerSync(event);
        inst.waitForIdleSync();
        eventTime = SystemClock.uptimeMillis();
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, 
                x + ViewConfiguration.getTouchSlop() / 2,
                y + ViewConfiguration.getTouchSlop() / 2, 0);
        inst.sendPointerSync(event);
        inst.waitForIdleSync();
        RobotiumUtils.sleep((int)(ViewConfiguration.getLongPressTimeout() * 1.5f));
        eventTime = SystemClock.uptimeMillis();
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, x, y, 0);
        inst.sendPointerSync(event);
        inst.waitForIdleSync();
		RobotiumUtils.sleep(500);

	}
	
	
	/**
	 * Method used to click on a specific view.
	 *
	 * @param view the view that should be clicked
	 *
	 */
	
	public void clickOnScreen(View view) {
		clickOnScreen(view, false);
	}
	
	/**
	 * Private method used to click on a specific view.
	 *
	 * @param view the view that should be clicked
	 * @param longClick true if the click should be a long click
	 *
	 */
	
	private void clickOnScreen(View view, boolean longClick) {
		int[] xy = new int[2];
		view.getLocationOnScreen(xy);
		final int viewWidth = view.getWidth();
		final int viewHeight = view.getHeight();
		final float x = xy[0] + (viewWidth / 2.0f);
		final float y = xy[1] + (viewHeight / 2.0f);
	
		if(longClick)
			clickLongOnScreen(x, y);
		else
			clickOnScreen(x, y);	
	}
	
	/**
	 * Method used to long click on a specific view.
	 *
	 * @param view the view that should be long clicked
	 *
	 */
	
	public void clickLongOnScreen(View view) {
		clickOnScreen(view, true);
		
	}
	
	/**
	 * This method is used to click on a specific text view displaying a certain
	 * text.
	 *
	 * @param text the text that should be clicked on. Regular expressions are supported
	 *
	 */
	
	public void clickOnText(String text) {
		clickOnText(text, false);
	}
	
	/**
	 * This method is used to long click on a specific text view and then selecting
	 * an item from the menu that appears.
	 *
	 * @param text the text that should be clicked on. Regular expressions are supported
	 * @param index the index of the menu item that should be pressed
	 *
	 */
	
	public void clickLongOnTextAndPress(String text, int index)
	{
		clickOnText(text, true);
		inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
		for(int i = 0; i < index; i++)
		{
			RobotiumUtils.sleep(300);
			inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
		}
		inst.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
	}
	
	
	/**
	 * Private method that is used to click on a specific text view displaying a certain
	 * text.
	 *
	 * @param text the text that should be clicked on. Regular expressions are supported
	 * @param longClick true if the click should be a long click 
	 *
	 */
	
	private void clickOnText(String text, boolean longClick) {
		Pattern p = Pattern.compile(text);
		Matcher matcher; 
		soloActivity.waitForIdle();
		boolean found = false;
		ArrayList <TextView> textViews = soloView.getCurrentTextViews(null);
		Iterator<TextView> iterator = textViews.iterator();
		TextView textView = null;
		while (iterator.hasNext()) {
			textView = iterator.next();
			matcher = p.matcher(textView.getText().toString());
			if(matcher.matches()){	
				found = true;
				break;
			}
		}
		if (found) {
			if (longClick)
				clickLongOnScreen(textView);
			else
				clickOnScreen(textView);
		} else if (soloScroll.scrollDownList()) {
			clickOnText(text, longClick);
		} else {
			for (int i = 0; i < textViews.size(); i++)
				Log.d(LOG_TAG, text + " not found. Have found: "
						+ textViews.get(i).getText());
			Assert.assertTrue("The text: " + text + " is not found!", false);
		}
	}
	
	
	
	/**
	 * This method is used to click on a button with a specific index.
	 *
	 * @param index the index number of the button
	 * @return true if button with specified index is found
	 *
	 */
	
	public boolean clickOnButton(int index) {
		soloActivity.waitForIdle();
		boolean found = false;
		Button button = null;
		try {
			button = soloView.getButton(index);
		} catch (IndexOutOfBoundsException e) {}
		if (button != null) {
			clickOnScreen(button);
			found = true;
		}
		return found;
	}
	

	
	/**
	 * Method used to click on a button with a given text.
	 *
	 * @param name the name of the button presented to the user. Regular expressions are supported
	 *
	 */
	
	public void clickOnButton(String name) {
		Pattern p = Pattern.compile(name);
		Matcher matcher;
		Button button = null;
		soloActivity.waitForIdle();
		boolean found = false;
		ArrayList<Button> buttonList = soloView.getCurrentButtons();
		Iterator<Button> iterator = buttonList.iterator();
		while (iterator.hasNext()) {
			button = iterator.next();
			matcher = p.matcher(button.getText().toString());
			if(matcher.matches()){	
				found = true;
				break;
			}
		}
		if (found) {
			clickOnScreen(button);
		} else if (soloScroll.scrollDownList()){
			clickOnButton(name);
		}else
		{
			for (int i = 0; i < buttonList.size(); i++)
				Log.d(LOG_TAG, name + " not found. Have found: " + buttonList.get(i).getText());
			Assert.assertTrue("Button with the text: " + name + " is not found!", false);
		}

	}
	
	/**
	 * This method is used to click on an image with a certain index.
	 *
	 * @param index the index of the image to be clicked
	 *
	 */
	
	public void clickOnImage(int index) {
		soloActivity.waitForIdle();
		try {
			clickOnScreen(soloView.getCurrentImageViews().get(index));
		} catch (IndexOutOfBoundsException e) {
			Assert.assertTrue("Index is not valid", false);
		}
	}
	
	/**
	 * Method used to simulate pressing the hard key back
	 * 
	 */
	
	public void goBack() {
		RobotiumUtils.sleep(300);
		try {
			inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
		} catch (Throwable e) {}
	}
	
	/**
	 * Private method that returns the list item parent. It is used by clickInList().
	 * 
	 * @param view the view who's parent is requested
	 * @return the parent of the view
	 */
	
	private View getListItemParent(View view)
	{
		if (view.getParent() != null
				&& !(view.getParent() instanceof android.widget.ListView)) {
			return getListItemParent((View) view.getParent());
		} else {
			return view;
		}
		
	}
	
	/**
	 * Method that will click on a certain list line and return the text views that
	 * the list line is showing. Will use the first list it finds.
	 * 
	 * @param line the line that should be clicked
	 * @return an array list of the text views located in the list line
	 */

	public ArrayList<TextView> clickInList(int line) {
		return clickInList(line, 0);
	}
	
	/**
	 * Method that will click on a certain list line on a specified List and 
	 * return the text views that the list line is showing. 
	 * 
	 * @param line the line that should be clicked
	 * @param index the index of the list. E.g. Index 1 if two lists are available
	 * @return an array list of the text views located in the list line
	 */
	
	public ArrayList<TextView> clickInList(int line, int index) {
        soloActivity.waitForIdle();
        if(soloView.getCurrentListViews().size()<index)
        	Assert.assertTrue("No ListView with index " + index + " is available", false);
        ArrayList<TextView> textViews = soloView.getCurrentTextViews(soloView
                .getCurrentListViews().get(index));
        ArrayList<TextView> textViewGroup = new ArrayList<TextView>();
        int myLine = 0;
        for (int i = 0; i < textViews.size(); i++) {
            View view = getListItemParent(textViews.get(i));
            try {
                if (view.equals(getListItemParent(textViews.get(i + 1)))) {
                	textViewGroup.add(textViews.get(i));
                } else {
                    textViewGroup.add(textViews.get(i));
                    myLine++;
                    if (myLine == line)
                        break;
                    else
                        textViewGroup.clear();
                }
            } catch (IndexOutOfBoundsException e) {}
        }
        if (textViewGroup.size() != 0)
            clickOnScreen(textViewGroup.get(0));
        return textViewGroup;
    }
}