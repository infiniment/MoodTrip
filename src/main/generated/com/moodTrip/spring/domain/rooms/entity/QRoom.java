package com.moodTrip.spring.domain.rooms.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRoom is a Querydsl query type for Room
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRoom extends EntityPathBase<Room> {

    private static final long serialVersionUID = -526323742L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRoom room = new QRoom("room");

    public final com.moodTrip.spring.global.common.entity.QBaseEntity _super = new com.moodTrip.spring.global.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.moodTrip.spring.domain.member.entity.QMember creator;

    public final BooleanPath isDeleteRoom = createBoolean("isDeleteRoom");

    public final NumberPath<Integer> roomCurrentCount = createNumber("roomCurrentCount", Integer.class);

    public final StringPath roomDescription = createString("roomDescription");

    public final NumberPath<Long> roomId = createNumber("roomId", Long.class);

    public final NumberPath<Integer> roomMaxCount = createNumber("roomMaxCount", Integer.class);

    public final ListPath<RoomMember, QRoomMember> roomMembers = this.<RoomMember, QRoomMember>createList("roomMembers", RoomMember.class, QRoomMember.class, PathInits.DIRECT2);

    public final StringPath roomName = createString("roomName");

    public final DatePath<java.time.LocalDate> travelEndDate = createDate("travelEndDate", java.time.LocalDate.class);

    public final DatePath<java.time.LocalDate> travelStartDate = createDate("travelStartDate", java.time.LocalDate.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QRoom(String variable) {
        this(Room.class, forVariable(variable), INITS);
    }

    public QRoom(Path<? extends Room> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRoom(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRoom(PathMetadata metadata, PathInits inits) {
        this(Room.class, metadata, inits);
    }

    public QRoom(Class<? extends Room> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.creator = inits.isInitialized("creator") ? new com.moodTrip.spring.domain.member.entity.QMember(forProperty("creator")) : null;
    }

}

