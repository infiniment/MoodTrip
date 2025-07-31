package com.moodTrip.spring.domain.rooms.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QEmotionRoom is a Querydsl query type for EmotionRoom
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEmotionRoom extends EntityPathBase<EmotionRoom> {

    private static final long serialVersionUID = -300721425L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QEmotionRoom emotionRoom = new QEmotionRoom("emotionRoom");

    public final com.moodTrip.spring.global.common.entity.QBaseEntity _super = new com.moodTrip.spring.global.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> emotionRoomId = createNumber("emotionRoomId", Long.class);

    public final QRoom room;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QEmotionRoom(String variable) {
        this(EmotionRoom.class, forVariable(variable), INITS);
    }

    public QEmotionRoom(Path<? extends EmotionRoom> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QEmotionRoom(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QEmotionRoom(PathMetadata metadata, PathInits inits) {
        this(EmotionRoom.class, metadata, inits);
    }

    public QEmotionRoom(Class<? extends EmotionRoom> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.room = inits.isInitialized("room") ? new QRoom(forProperty("room")) : null;
    }

}

