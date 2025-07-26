package com.moodTrip.spring.domain.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMember is a Querydsl query type for Member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMember extends EntityPathBase<Member> {

    private static final long serialVersionUID = -278262817L;

    public static final QMember member = new QMember("member1");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    public final BooleanPath isWithdraw = createBoolean("isWithdraw");

    public final StringPath memberAuth = createString("memberAuth");

    public final StringPath memberId = createString("memberId");

    public final StringPath memberName = createString("memberName");

    public final StringPath memberPhone = createString("memberPhone");

    public final NumberPath<Long> memberPk = createNumber("memberPk", Long.class);

    public final StringPath memberPw = createString("memberPw");

    public final NumberPath<Long> rptCnt = createNumber("rptCnt", Long.class);

    public final NumberPath<Long> rptRcvdCnt = createNumber("rptRcvdCnt", Long.class);

    public final StringPath socialType = createString("socialType");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QMember(String variable) {
        super(Member.class, forVariable(variable));
    }

    public QMember(Path<? extends Member> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMember(PathMetadata metadata) {
        super(Member.class, metadata);
    }

}

