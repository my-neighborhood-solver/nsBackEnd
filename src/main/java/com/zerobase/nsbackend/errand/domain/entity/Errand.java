package com.zerobase.nsbackend.errand.domain.entity;

import com.zerobase.nsbackend.errand.domain.vo.ErrandStatus;
import com.zerobase.nsbackend.errand.domain.vo.PayDivision;
import com.zerobase.nsbackend.global.BaseTimeEntity;
import com.zerobase.nsbackend.member.domain.Member;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Errand extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne
  private Member errander;    // 의뢰자
  @Column(length = 500)
  private String title;
  @Column(columnDefinition = "TEXT")
  private String content;
  @Builder.Default
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "ERRAND_ID")
  private List<ErrandImage> images = new ArrayList<>();
  @Enumerated(EnumType.STRING)
  private PayDivision payDivision;
  private Integer pay;
  @Enumerated(EnumType.STRING)
  private ErrandStatus status;
  private Integer viewCount;

  public void edit(String title, String content, PayDivision payDivision, Integer pay) {
    this.title = title;
    this.content = content;
    this.payDivision = payDivision;
    this.pay = pay;
  }

  public void changeStatus(ErrandStatus status) {
    this.status = status;
  }
}
