package com.moodTrip.spring.domain.emotion.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QEmotion is a Querydsl query type for Emotion
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEmotion extends EntityPathBase<Emotion> {

    private static final long serialVersionUID = 1254890673L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QEmotion emotion = new QEmotion("emotion");

    public final com.moodTrip.spring.global.common.entity.QBaseEntity _super = new com.moodTrip.spring.global.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> displayOrder = createNumber("displayOrder", Integer.class);

    public final QEmotionCategory emotionCategory;

    public final NumberPath<Integer> tagId = createNumber("tagId", Integer.class);

    public final StringPath tagName = createString("tagName");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QEmotion(String variable) {
        this(Emotion.class, forVariable(variable), INITS);
    }

    public QEmotion(Path<? extends Emotion> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QEmotion(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QEmotion(PathMetadata metadata, PathInits inits) {
        this(Emotion.class, metadata, inits);
    }

    public QEmotion(Class<? extends Emotion> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.emotionCategory = inits.isInitialized("emotionCategory") ? new QEmotionCategory(forProperty("emotionCategory")) : null;
    }

}

