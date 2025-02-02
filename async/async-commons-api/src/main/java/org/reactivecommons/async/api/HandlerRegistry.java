package org.reactivecommons.async.api;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.reactivecommons.async.api.handlers.CommandHandler;
import org.reactivecommons.async.api.handlers.EventHandler;
import org.reactivecommons.async.api.handlers.QueryHandler;
import org.reactivecommons.async.api.handlers.QueryHandlerDelegate;
import org.reactivecommons.async.api.handlers.registered.RegisteredCommandHandler;
import org.reactivecommons.async.api.handlers.registered.RegisteredEventListener;
import org.reactivecommons.async.api.handlers.registered.RegisteredQueryHandler;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class HandlerRegistry {

    private final List<RegisteredEventListener<?>> eventListeners = new CopyOnWriteArrayList<>();
    private final List<RegisteredEventListener<?>> dynamicEventHandlers = new CopyOnWriteArrayList<>();
    private final List<RegisteredEventListener<?>> eventNotificationListener = new CopyOnWriteArrayList<>();
    private final List<RegisteredQueryHandler<?, ?>> handlers = new CopyOnWriteArrayList<>();
    private final List<RegisteredCommandHandler<?>> commandHandlers = new CopyOnWriteArrayList<>();

    public static HandlerRegistry register() {
        return new HandlerRegistry();
    }

    public <T> HandlerRegistry listenEvent(String eventName, EventHandler<T> handler, Class<T> eventClass) {
        eventListeners.add(new RegisteredEventListener<>(eventName, handler, eventClass));
        return this;
    }

    public <T> HandlerRegistry listenEvent(String eventName, EventHandler<T> handler) {
        return listenEvent(eventName, handler, inferGenericParameterType(handler));
    }

    public <T> HandlerRegistry listenNotificationEvent(String eventName, EventHandler<T> handler, Class<T> eventClass) {
        eventNotificationListener.add(new RegisteredEventListener<>(eventName, handler, eventClass));
        return this;
    }

    public <T> HandlerRegistry handleDynamicEvents(String eventNamePattern, EventHandler<T> handler, Class<T> eventClass) {
        dynamicEventHandlers.add(new RegisteredEventListener<>(eventNamePattern, handler, eventClass));
        return this;
    }

    public <T> HandlerRegistry handleDynamicEvents(String eventNamePattern, EventHandler<T> handler) {
        return handleDynamicEvents(eventNamePattern, handler, inferGenericParameterType(handler));
    }

    public <T> HandlerRegistry handleCommand(String commandName, CommandHandler<T> fn, Class<T> commandClass) {
        commandHandlers.add(new RegisteredCommandHandler<>(commandName, fn, commandClass));
        return this;
    }

    public <T> HandlerRegistry handleCommand(String commandName, CommandHandler<T> fn) {
        commandHandlers.add(new RegisteredCommandHandler<>(commandName, fn, inferGenericParameterType(fn)));
        return this;
    }

    public <T, R> HandlerRegistry serveQuery(String resource, QueryHandler<T, R> handler) {
        return serveQuery(resource, handler, inferGenericParameterType(handler));
    }

    public <T, R> HandlerRegistry serveQuery(String resource, QueryHandler<T, R> handler, Class<R> queryClass) {
        handlers.add(new RegisteredQueryHandler<>(resource, (ignored, message) -> handler.handle(message), queryClass));
        return this;
    }

    public <R> HandlerRegistry serveQuery(String resource, QueryHandlerDelegate<Void, R> handler, Class<R> queryClass) {
        handlers.add(new RegisteredQueryHandler<>(resource, handler, queryClass));
        return this;
    }


    @SuppressWarnings("unchecked")
    private <T, R> Class<R> inferGenericParameterType(QueryHandler<T, R> handler) {
        try {
            ParameterizedType genericSuperclass = (ParameterizedType) handler.getClass().getGenericInterfaces()[0];
            return (Class<R>) genericSuperclass.getActualTypeArguments()[1];
        } catch (Exception e) {
            throw new RuntimeException("Fail to infer generic Query class, please use serveQuery(path, handler, " +
                    "class) instead");
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> inferGenericParameterType(CommandHandler<T> handler) {
        try {
            ParameterizedType genericSuperclass = (ParameterizedType) handler.getClass().getGenericInterfaces()[0];
            return (Class<T>) genericSuperclass.getActualTypeArguments()[0];
        } catch (Exception e) {
            throw new RuntimeException("Fail to infer generic Command class, please use handleCommand(path, handler, " +
                    "class) instead");
        }
    }

    private <T> Class<T> inferGenericParameterType(EventHandler<T> handler) {
        try {
            ParameterizedType genericSuperclass = (ParameterizedType) handler.getClass().getGenericInterfaces()[0];
            return (Class<T>) genericSuperclass.getActualTypeArguments()[0];
        } catch (Exception e) {
            throw new RuntimeException("Fail to infer generic Query class, please use listenEvent(eventName, handler," +
                    " class) instead");
        }
    }
}



