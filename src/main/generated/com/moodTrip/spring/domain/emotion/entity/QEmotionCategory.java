package com.moodTrip.spring.domain.emotion.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QEmotionCategory is a Querydsl query type for EmotionCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEmotionCategory extends EntityPathBase<EmotionCategory> {

    private static final long serialVersionUID = -987890481L;

    public static final QEmotionCategory emotionCategory = new QEmotionCategory("emotionCategory");

    public final com.moodTrip.spring.global.common.entity.QBaseEntity _super = new com.moodTrip.spring.global.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> displayOrder = createNumber("displayOrder", Integer.class);

    public final StringPath emotionCategoryIcon = createString("emotionCategoryIcon");

    public final NumberPath<Long> emotionCategoryId = createNumber("emotionCategoryId", Long.class);

    public final StringPath emotionCategoryName = createString("emotionCategoryName");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QEmotionCategory(String variable) {
        super(EmotionCategory.class, forVariable(variable));
    }

    public QEmotionCategory(Path<? extends EmotionCategory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEmotionCategory(PathMetadata metadata) {
        super(EmotionCategory.class, metadata);
    }

}

