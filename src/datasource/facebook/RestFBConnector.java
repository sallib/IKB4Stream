package facebook;
import com.restfb.*;
import com.restfb.types.Event;
import com.waves_rsp.ikb4stream.core.model.LatLong;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


/**
 * RestFBConnector class provides events link to a word form coordinates
 */
public class RestFBConnector {
    private final Logger LOGGER = LoggerFactory.getLogger(RestFBConnector.class);
    private final String source;
    private final String pageAccessToken;

    public RestFBConnector() {
        try {
            PropertiesManager propertiesManager = PropertiesManager.getInstance(RestFBConnector.class, "resources/config.properties");
            this.source = propertiesManager.getProperty("FacebookProducerConnector.source");
            this.pageAccessToken = propertiesManager.getProperty("FacebookProducerConnector.token");
            System.out.println(source);
            System.out.println(pageAccessToken);
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage());
            throw new IllegalArgumentException("Invalid configuration");
        }
    }

    /**
     * @param word      a Sting which is the event to find
     * @param limit     an int which the result limit
     * @param latitude  a long
     * @param longitude a long
     * @return a list of events form Facebook events and coodinates
     */

    public List<com.waves_rsp.ikb4stream.core.model.Event> SearchWordFromGeolocation(String word, int limit, double latitude, double longitude) {
        Objects.requireNonNull(word);
        if (word != null) {
            List<com.waves_rsp.ikb4stream.core.model.Event> events = new ArrayList<>();
            FacebookClient facebookClient = new DefaultFacebookClient(this.pageAccessToken, Version.LATEST);
            Connection<Event> publicSearch = facebookClient.fetchConnection("search", Event.class,
                    Parameter.with("q", word),
                    Parameter.with("type", "event"),
                    Parameter.with("limit", limit),
                    Parameter.with("place&center", latitude + "," + longitude));
            for (int i = 0; i < publicSearch.getData().size(); i++) {
                if (isValidEvent(publicSearch.getData().get(i))) {
                    LatLong ll = new LatLong(publicSearch.getData().get(i).getPlace().getLocation().getLatitude(), publicSearch.getData().get(i).getPlace().getLocation().getLongitude());
                    Date start = publicSearch.getData().get(i).getStartTime();
                    Date end = publicSearch.getData().get(i).getEndTime();
                    String description = publicSearch.getData().get(i).getDescription();
                    events.add(new com.waves_rsp.ikb4stream.core.model.Event(ll, start, end, description, source));
                }
            }
            return events;
        }
        return null;
    }

    /**
     * Check if an event is valid i.e parameters are correctly set
     * @param event
     * @return True if valid
     */
    public boolean isValidEvent(Event event) {
        return (event != null) && (event.getPlace() != null)
                && (event.getPlace().getLocation() != null)
                && (event.getPlace().getLocation().getLongitude() != null)
                && (event.getPlace().getLocation().getLatitude() != null)
                && (event.getStartTime() != null)
                && (event.getDescription() != null)
                && (event.getEndTime() != null);
    }
}
