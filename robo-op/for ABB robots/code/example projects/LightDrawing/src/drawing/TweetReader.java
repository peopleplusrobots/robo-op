package drawing;

import java.util.ArrayList;

import twitter4j.MediaEntity;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;
import twitter4j.conf.ConfigurationBuilder;

/**
 * TweetReader
 * Getting tweets from twitter based on users or search terms.
 *
 * Twitter code from Jer Thorpe's tutorial:
 *    http://blog.blprnt.com/blog/blprnt/updated-quick-tutorial-processing-twitter
 * Updated for twitter4j 4.0.2 (Tweet objects are now Status Objects)
 *
 * @author mad Ð 11.12.2014
 */
public class TweetReader {
	
	private ConfigurationBuilder cb;
	private Twitter twitter;

	// Twitter oAuth stuff ... taken from the "Manage Apps" page of dev.twitter.com
	private static final String CONSUMER_KEY        = "hPPFrBezXdmMJmiGHDsItRjro";
	private static final String CONSUMER_SECRET     = "hOcFWHFX4muCtyoGrkXtUeDZFF0A2x4Q0b5cOGkXyuKHk8ul5Z";
	private static final String ACCESS_TOKEN        = "236562313-GlTz1MinsFwBuUgofBvGUB9IQ9FYMOrNwjYzW4FL";
	private static final String ACCESS_TOKEN_SECRET = "2rBnIpZWbnGnKhLjfDoGNbsNnheiTOKLacLtnPKAcwAph";

	
	
	public TweetReader(){
		cb = new ConfigurationBuilder();
		cb.setOAuthConsumerKey(CONSUMER_KEY);
		cb.setOAuthConsumerSecret(CONSUMER_SECRET);
		cb.setOAuthAccessToken(ACCESS_TOKEN);
		cb.setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET);

		twitter = new TwitterFactory(cb.build()).getInstance();
	}

	/**
	 * Sends tweet from @madelinegannon account to another user.
	 * 
	 * @param user
	 * @param msg
	 */
	public void tweetAt(String user, String msg){		
		try {
			twitter.updateStatus(user+" "+msg);
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Searches for tweets with a particular keyword.
	 * 
	 * @param keyword
	 * @param count
	 * @return List of each tweet as a list of words.
	 */
	public ArrayList<ArrayList<String>> searchFor(String keyword, int count){
		
		ArrayList<Status> tweets = new ArrayList<Status>();
		
		Query query = new Query(keyword);
		query.setCount(count);

		try {
			QueryResult result = twitter.search(query);
			tweets = (ArrayList<Status>) result.getTweets();		
		}catch (TwitterException te) {
			System.out.println("Couldn't connect: " + te);
		}	
		
		return parseTweets(tweets);		
	}
	
	/**
	 * Returns a list of tweets from a user.
	 * 
	 * @param user
	 * @return - List of each tweet as a list of words, from a giving user.
	 */
	public ArrayList<ArrayList<String>> getTweetsFromUser(String user){
		
		ArrayList<ArrayList<String>> tweets = new ArrayList<ArrayList<String>>();
		
		// get the list of past tweets from a user
		ResponseList<Status> statuses;
		try {
			statuses = twitter.getUserTimeline(user);
//			for (Status s : statuses){
			Status s = statuses.get(0);
				tweets.add(parseTweet(s));
//			}
					
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		
		return tweets;
	}
	
	/**
	 * Get the parsed text from each status.
	 * 
	 * @param statuses
	 * @return List of each tweet as a list of words.
	 */
	private ArrayList<ArrayList<String>> parseTweets(ArrayList<Status> statuses){
		ArrayList<ArrayList<String>> tweets = new ArrayList<ArrayList<String>>();
		
		for (Status s : statuses)
			tweets.add(parseTweet(s));
		
		return tweets;
	}
	
	/**
	 * Removing URLs and RTs from tweet text.
	 * 
	 * @param s - status object for tweet
	 * @return List of each word in tweet.
	 */
	private ArrayList<String> parseTweet(Status s) {
		
		String originalTweet = s.getText();
		String tweet = originalTweet;
		
		// remove urls from tweet
		for (URLEntity entity : s.getURLEntities())
			tweet = tweet.replace(entity.getText(),"");
		for (MediaEntity entity : s.getMediaEntities())
			tweet = tweet.replace(entity.getURL(),"");
		
		// remove RTs
		for (UserMentionEntity ume : s.getUserMentionEntities()){
			if (tweet.contains("RT @"+ume.getText()+": "))
				tweet = tweet.replace("RT @"+ume.getText()+": ","");
		}
		
		// remove any extra spaces
		String[] words = tweet.split(" ");
		ArrayList<String> msg = new ArrayList<String>();
		for (int i=0; i<words.length; i++){
			if (!words[i].equals(""))
				msg.add(words[i]);									
		}
		System.out.println();
		System.out.println();
		System.out.println(msg);
		return msg;
	}
	
}
