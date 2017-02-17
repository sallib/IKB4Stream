package com.waves_rsp.ikb4stream.producer.score;

import com.waves_rsp.ikb4stream.core.datasource.model.IScoreProcessor;
import com.waves_rsp.ikb4stream.core.model.Event;
import com.waves_rsp.ikb4stream.core.model.PropertiesManager;
import com.waves_rsp.ikb4stream.core.util.JarLoader;
import com.waves_rsp.ikb4stream.core.util.UtilManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.stream.Stream;

public class ScoreProcessorManager {
    private static final PropertiesManager PROPERTIES_MANAGER = PropertiesManager.getInstance(ScoreProcessorManager.class, "resources/config.properties");
    private static final Logger LOGGER = LoggerFactory.getLogger(ScoreProcessorManager.class);
    private final ClassLoader parent = ScoreProcessorManager.class.getClassLoader();
    private final Map<List<String>,IScoreProcessor> scoreProcessors = new HashMap<>();

    /**
     * Override default constructor
     */
    public ScoreProcessorManager() {
        // Do nothing more
    }

    /**
     * Process NLP Algorithm to an event
     * @param event Event to score
     * @return Copy of {@param event} with a new score
     * @throws NullPointerException if {@param event} is null
     */
    public Event processScore(Event event) {
        Objects.requireNonNull(event);
        List<IScoreProcessor> sp = findIScoreProcessor(event.getSource());
        return process(sp, event);
    }

    /**
     * Find ScoreProcessor to apply to a datasource
     * @param source Origin of the datasource
     * @return List of all ScoreProcessor to apply
     * @throws NullPointerException if {@param source} is null
     */
    private List<IScoreProcessor> findIScoreProcessor(String source) {
        Objects.requireNonNull(source);
        List<IScoreProcessor> score = new ArrayList<>();
        scoreProcessors.keySet().stream()
                .filter(list -> list.contains(source))
                .forEach(list -> score.add(scoreProcessors.get(list)));
        return score;
    }

    /**
     * Apply all ScoreProcessor to the Event
     * @param scoreProcessor List of all scoreprocessor to apply
     * @param event Event to process
     * @return Copy of {@param event} with its score process
     * @throws NullPointerException if {@param scoreProcessor} or {@param event} is null
     */
    private static Event process(List<IScoreProcessor> scoreProcessor, Event event) {
        Objects.requireNonNull(scoreProcessor);
        Objects.requireNonNull(event);
        Event tmp = event;
        for (IScoreProcessor sp : scoreProcessor) {
            tmp = sp.processScore(tmp);
        }
        return tmp;
    }

    /**
     * Get all ScoreProcessor
     */
    public void instanciate() {
        String stringPath = PROPERTIES_MANAGER.getProperty("scoreprocessor.path");
        try (Stream<Path> paths = Files.walk(Paths.get(stringPath))) {
            paths.forEach((Path filePath) -> {
                if (Files.isRegularFile(filePath)) {
                    JarLoader jarLoader = JarLoader.createJarLoader(filePath.toString());
                    String jarName = filePath.getFileName().toString();
                    launchModule(jarName, jarLoader);
                }
            });
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Launch module of ScoreProcessor
     * @param jarLoader JarLoader that represents module
     */
    private void launchModule(String jarName, JarLoader jarLoader) {
        if (jarLoader != null) {
            List<URL> urls = jarLoader.getUrls();
            AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                ClassLoader classLoader = new URLClassLoader(
                        urls.toArray(new URL[urls.size()]),
                        parent);
                List<String> classes = jarLoader.getClasses();
                classes.stream()
                        .map(c -> UtilManager.loadClass(c, classLoader))
                        .filter(c -> UtilManager.implementInterface(c, IScoreProcessor.class))
                        .forEach(clazz -> {
                            IScoreProcessor iScoreProcessor = (IScoreProcessor) UtilManager.newInstance(clazz);
                            scoreProcessors.put(iScoreProcessor.getSources(), iScoreProcessor);
                            LOGGER.info("ScoreProcessor " + jarName + " has been launched");
                        });

                return null;
            });
        }
    }
}
