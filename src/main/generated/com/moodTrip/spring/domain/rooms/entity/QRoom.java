package com.moodTrip.spring.domain.rooms.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QRoom is a Querydsl query type for Room
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRoom extends EntityPathBase<Room> {

    private static final long serialVersionUID = -526323742L;

    public static final QRoom room = new QRoom("room");

    public final com.moodTrip.spring.global.common.entity.QBaseEntity _super = new com.moodTrip.spring.global.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final BooleanPath isDeleteRoom = createBoolean("isDeleteRoom");

    public final NumberPath<Integer> roomCurrentCount = createNumber("roomCurrentCount", Integer.class);

    public final StringPath roomDescription = createString("roomDescription");

    public final NumberPath<Long> roomId = createNumber("roomId", Long.class);

    public final NumberPath<Integer> roomMaxCount = createNumber("roomMaxCount", Integer.class);

    public final StringPath roomName = createString("roomName");

    public final DatePath<java.time.LocalDate> travelEndDate = createDate("travelEndDate", java.time.LocalDate.class);

    public final DatePath<java.time.LocalDate> travelStartDate = createDate("travelStartDate", java.time.LocalDate.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QRoom(String variable) {
        super(Room.class, forVariable(variable));
    }

    public QRoom(Path<? extends Room> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRoom(PathMetadata metadata) {
        super(Room.class, metadata);
    }

}

