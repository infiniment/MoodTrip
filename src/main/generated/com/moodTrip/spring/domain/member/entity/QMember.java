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

    public final com.moodTrip.spring.global.common.entity.QBaseEntity _super = new com.moodTrip.spring.global.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath email = createString("email");

    public final BooleanPath isWithdraw = createBoolean("isWithdraw");

    public final StringPath memberAuth = createString("memberAuth");

    public final StringPath memberId = createString("memberId");

    public final StringPath memberPhone = createString("memberPhone");

    public final NumberPath<Long> memberPk = createNumber("memberPk", Long.class);

    public final StringPath memberPw = createString("memberPw");

    public final StringPath nickname = createString("nickname");

    public final StringPath provider = createString("provider");

    public final StringPath providerId = createString("providerId");

    public final NumberPath<Long> rptCnt = createNumber("rptCnt", Long.class);

    public final NumberPath<Long> rptRcvdCnt = createNumber("rptRcvdCnt", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

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

