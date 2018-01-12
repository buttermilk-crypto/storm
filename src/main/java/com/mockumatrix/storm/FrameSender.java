/*
 * Copyright 2017 David R. Smith (@mockumatrix) All Rights Reserved
 */

package com.mockumatrix.storm;

import java.io.File;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import twitter4j.RateLimitStatus;
import twitter4j.RateLimitStatusEvent;
import twitter4j.RateLimitStatusListener;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.UploadedMedia;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Send our frame (set of tweet data). Do not resend if we had previously done so. 
 * 
 * @author Dave
 *
 */
public class FrameSender implements Runnable {

	Display display;
	StormFrame frame;
	AccountManager accountManager;
	PropertiesManager propsManager;
	Text outputText;
	boolean pinToProfile;

	Twitter twitter;
	
	boolean kill;
	
	static final int delay = 45;

	public FrameSender(Display display, StormFrame frame, AccountManager accountManager, PropertiesManager propsManager, Text outputText) {
		super();
		this.display = display;
		this.frame = frame;
		this.propsManager = propsManager;
		this.accountManager = accountManager;
		this.outputText = outputText;
	}

	public FrameSender configure() {
		kill = false;
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setJSONStoreEnabled(true);
		
		// data present in storm.json file
		cb.setOAuthAccessToken(accountManager.getSelectedAccount().get("accessToken"));
		cb.setOAuthAccessTokenSecret(accountManager.getSelectedAccount().get("accessTokenSecret"));
		cb.setOAuthConsumerKey(accountManager.getSelectedAccount().get("consumerKey"));
		cb.setOAuthConsumerSecret(accountManager.getSelectedAccount().get("consumerSecret"));

		twitter = new TwitterFactory(cb.build()).getInstance();

		// RATE LIMITS HANDLING - if it looks like we are hitting a limit, pause
		twitter.addRateLimitStatusListener(new RateLimitStatusListener() {

			@Override
			public void onRateLimitStatus(RateLimitStatusEvent event) {
				RateLimitStatus stat = event.getRateLimitStatus();
				msg("Limit:" + stat.getRemaining() + "/" + stat.getLimit());
			}

			@Override
			public void onRateLimitReached(RateLimitStatusEvent event) {
				RateLimitStatus stat = event.getRateLimitStatus();
				int secReset = stat.getSecondsUntilReset();
				msg("Hit rate limit, sleeping for " + secReset + " seconds. Click stop if you want.");
				try {
					Thread.sleep((secReset * 1000) + 5000);
				} catch (InterruptedException e) {
				//	e.printStackTrace();
				}
			}
		});

		return this;

	}
	
	private void msg(String msg) {
		
		display.asyncExec(new Runnable() {
            @Override
            public void run() {
                if (outputText == null || outputText.isDisposed ())
                    return;
                outputText.append(msg+"\n");
            }
        });
	}

	public void run() {
		try {	
			if (frame.entries.get(0).tweetId == 0) {
				// frame does not appear to have been sent at all, assume new.
				sendAsNew();
			} else {
				sendAsPartial();
			}
		}catch(Exception x) {
			x.printStackTrace();
		}
		
		msg("Thread complete.");
		
	}
	
	public void stop() {
		kill = true;
	}

	public void sendAsPartial() {
		
		long topLevelId = frame.entries.get(0).tweetId;
		
		try {
			// now send our tweet storm as replies one per minute
			// we'll skip any who have what look to be valid tweet ids.
			// TO DO, check if the existing ids are valid ?
			boolean sentAnything = false;
			for(int i = 1; i<frame.entries.size(); i++){
				
				if(kill) return;
				
				StormEntry s = frame.entries.get(i);
				
				if(s.tweetId == 0){
					
				    StatusUpdate update = new StatusUpdate(s.tweetText);
				 // send multiple media files first, and correlate ids. 
				    long [] mediaIds = null;
					if (s.attachmentPaths.size() > 0) {
						 mediaIds = new long[s.attachmentPaths.size()];
				         for(int j=0; j<s.attachmentPaths.size(); j++) {
				        	 if(kill) return;
				        	msg("Uploading media: "+s.attachmentPaths.get(j));
				            UploadedMedia media = twitter.uploadMedia(new File(s.attachmentPaths.get(j)));
				            mediaIds[j] = media.getMediaId();
				         }
				         
				         update.setMediaIds(mediaIds);
					}
				
				    update.setInReplyToStatusId(topLevelId);
				    
				    Status status = twitter.updateStatus(update);
				    msg("sent update: id="+status.getId()+" "+s.getTweetText());
				    s.tweetId = status.getId();
				    frame.save();
				    sentAnything = true;
				    try {
				       if(kill) return;
 				       msg("Sleeping now for "+delay+" seconds...");
					   Thread.sleep(delay*1000);
				    } catch (InterruptedException e) {
					 //   e.printStackTrace();
				    }
				}else {
					msg("Skipping "+s.tweetId+", "+s.tweetText);
				}
			   }
			
			if(!sentAnything) {
			    msg("Did not update any entries...");
			}else {
				msg("Done!");
			}
			
		} catch (TwitterException e) {
			e.printStackTrace();
		}

	}

	public void sendAsNew() {

		StormEntry top = frame.entries.get(0);

		// Initially create one status, get the ID
		try {
			// Status status = twitter.updateStatus(top.tweetText);
			StatusUpdate update = new StatusUpdate(top.tweetText);

			long[] mediaIds = null;
			if (top.attachmentPaths.size() > 0) {
				 mediaIds = new long[top.attachmentPaths.size()];
		         for(int i=0; i<top.attachmentPaths.size(); i++) {
		        	 if(kill) return;
		        	msg("Uploading media: "+top.attachmentPaths.get(i));
		            UploadedMedia media = twitter.uploadMedia(new File(top.attachmentPaths.get(i)));
		            mediaIds[i] = media.getMediaId();
		         }
		         
		         update.setMediaIds(mediaIds);
			}
			
			Status status = twitter.updateStatus(update);

			long id = status.getId();
			msg("established top level " + id + " " + top.getTweetText());
			top.setTweetId(id);
			frame.save();

			// now send our tweet storm as replies one per minute
			for (int i = 1; i < frame.entries.size(); i++) {
				
				if(kill) return;
				
				StormEntry s = frame.entries.get(i);
				update = new StatusUpdate(s.tweetText);

				mediaIds = null;
				if (s.attachmentPaths.size() > 0) {
					 mediaIds = new long[s.attachmentPaths.size()];
			         for(int j=0; j<s.attachmentPaths.size(); j++) {
			        	 if(kill) return;
			        	msg("Uploading media: "+s.attachmentPaths.get(j));
			            UploadedMedia media = twitter.uploadMedia(new File(s.attachmentPaths.get(j)));
			            mediaIds[j] = media.getMediaId();
			         }
			         
			         update.setMediaIds(mediaIds);
				}

				update.setInReplyToStatusId(id);
				status = twitter.updateStatus(update);
				msg("sent update: " + status.getId() + " " + s);
				s.tweetId = status.getId();
				frame.save();
				try {
					if(kill) return;
					   msg("Sleeping now for "+delay+" seconds...");
					   Thread.sleep(delay*1000);
				} catch (InterruptedException e) {
				//	e.printStackTrace();
				}
			}
				

		} catch (TwitterException e) {
			e.printStackTrace();
		}

		 msg("Done!");
	}

	public boolean isPinToProfile() {
		return pinToProfile;
	}

	public void setPinToProfile(boolean pinToProfile) {
		this.pinToProfile = pinToProfile;
	}

}
