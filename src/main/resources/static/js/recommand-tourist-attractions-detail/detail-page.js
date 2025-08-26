// -------------------- 유틸 --------------------
const FallbackImg = "/static/image/recommand-tourist-attractions-detail/SaryeoniForestTrail.png";
const $ = (s) => document.querySelector(s);

function escapeHtml(str) {
  return String(str)
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#39;");
}

function toHtml(txt) {
  const esc = escapeHtml(txt);
  return "<p>" + esc.replace(/\n{2,}/g, "</p><p>").replace(/\n/g, "<br>") + "</p>";
}

function splitOverview(text, { minChars = 350, minParas = 2 } = {}) {
  const paras = text.split(/\n{2,}/);
  const needToggle = paras.length > minParas || text.length > minChars;
  if (!needToggle) return { previewHtml: toHtml(text), restHtml: "", hasMore: false };

  let acc = "", i = 0;
  while (i < paras.length && acc.length < minChars) {
    acc += (i ? "\n\n" : "") + paras[i++];
  }
  const rest = paras.slice(i).join("\n\n");
  return { previewHtml: toHtml(acc), restHtml: toHtml(rest), hasMore: rest.length > 0 };
}

function renderOverview(overview) {
  const section = $("#overviewSection");
  const preview = $("#overviewPreview");
  const moreSec = $("#overviewMoreSection");
  const moreBody = $("#overviewMoreBody");
  const toggle = $("#overviewToggle");
  if (!section || !preview || !moreSec || !moreBody || !toggle) return;

  const text = (overview || "").replace(/\r\n?/g, "\n").trim();
  if (!text) {
    const wrapper = section.closest(".place-detail-wrapper");
    if (wrapper) wrapper.classList.add("hidden");
    moreSec.classList.add("hidden");
    toggle.classList.add("hidden");
    return;
  }

  const { previewHtml, restHtml, hasMore } = splitOverview(text, { minChars: 350, minParas: 2 });
  preview.innerHTML = previewHtml;
  moreBody.innerHTML = restHtml;

  if (hasMore) {
    toggle.classList.remove("hidden");
    moreSec.classList.add("hidden");
    toggle.addEventListener("click", () => {
      const nowHidden = moreSec.classList.toggle("hidden");
      toggle.querySelector(".toggle-text").textContent = nowHidden ? "더 자세히 보기" : "접기";
      toggle.querySelector(".toggle-icon").textContent = nowHidden ? "▼" : "▲";
    });
  } else {
    toggle.classList.add("hidden");
    moreSec.classList.add("hidden");
  }
}

function setMultiline(sel, v, fallback = "제공되지 않음") {
  const el = typeof sel === "string" ? $(sel) : sel;
  if (!el) return;
  const raw = (v ?? "").toString().trim();
  if (!raw) {
    el.textContent = fallback;
    return;
  }
  const txt = raw.replace(/<br\s*\/?>/gi, "\n").replace(/&nbsp;/gi, " ");
  el.textContent = txt;
  el.style.whiteSpace = "pre-line";
}

function setText(sel, v, fallback = "제공되지 않음") {
  const el = typeof sel === "string" ? $(sel) : sel;
  if (!el) return;
  const val = (v ?? "").toString().trim();
  el.textContent = val.length ? val : fallback;
}

// -------------------- 상세 데이터 바인딩 --------------------
document.addEventListener("DOMContentLoaded", async function () {
  const SSR = window.__SSR_DETAIL__ || null;
  let d = SSR;

  // SSR이 없는 경우에만 초기 텍스트 비움(덮어쓰기 티 방지)
  if (!SSR) {
    const nameEl = $(".place-name");
    if (nameEl) nameEl.textContent = "";
    ["#infoTel", "#infoAddr", "#infoUseTime", "#infoRest", "#infoParking", "#infoAge"].forEach(id => {
      const el = $(id);
      if (el) el.textContent = "";
    });
  }

  // 1) contentId 추출 (쿼리 우선, 버튼 data 폴백)
  let contentId = null;
  if (!d) {
    const params = new URLSearchParams(location.search);
    const pathId = location.pathname.match(/\/attractions\/detail\/(\d+)/)?.[1];
    contentId = params.get("contentId")
    ($("#btnMakeRoom")?.dataset.contentId ?? "")
    pathId;
    if (!contentId) return;

    // 2) API 호출
    try {
      const res = await fetch(`/api/attractions/content/${encodeURIComponent(contentId)}/detail`, {
        headers: { Accept: "application/json" }
      });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      d = await res.json();
    } catch (e) {
      console.error("상세 불러오기 실패:", e);
      return;
    }
  }

  // 3) 헤더/이미지/제목
  setText(".place-name", d.title || "상세정보");
  document.title = (d.title || "상세정보") + " - 상세정보";
  const hero = document.querySelector(".place-image img.place");
  if (hero) {
    hero.src = d.image || FallbackImg;
    hero.alt = d.title || "이미지";
    hero.onerror = () => { hero.onerror = null; hero.src = FallbackImg; };
  }

  // 4) 상세 정보 그리드
  setMultiline("#infoTel", d.tel);
  setMultiline("#infoAddr", d.addr);
  setMultiline("#infoUseTime", d.useTime);
  setMultiline("#infoRest", d.restDate);
  setMultiline("#infoParking", d.parking);
  setText("#infoAge", d.age);

  // 5) 개요(상세설명) — 길면 자동 2파트 + 토글
  renderOverview(d.overview);
});

// -------------------- (기존) 리뷰 UI --------------------
document.addEventListener("DOMContentLoaded", function () {
  const currentUser = { name: "김치국밥", isLoggedIn: true };

  const reviewDB = [];
  const reviewContainer = $("#reviewContainer");
  const reviewCountEl = $("#reviewcount");
  const ratingInput = $("#ratingValue");
  const stars = document.querySelectorAll(".star");
  const starContainer = $("#starRating");
  const moreBtn = document.querySelector(".review-more");

  let showingAll = false;

  function maskUsername(name) {
    const len = name.length;
    const visible = Math.ceil(len / 2);
    return name.slice(0, visible) + "*".repeat(len - visible);
  }

  function formatDate(date) {
    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    return `${year}.${month}.${day}`;
  }

  function updateReviewCount() {
    if (reviewCountEl) reviewCountEl.textContent = `총 ${reviewDB.length}개의 후기`;
  }

  function updateStars(score) {
    stars.forEach((star, index) => {
      star.classList.remove("full", "half", "empty");
      const i = index + 1;
      if (score >= i) star.classList.add("full");
      else if (score >= i - 0.5) star.classList.add("half");
      else star.classList.add("empty");
    });
  }

  function renderReviews(limit = 3) {
    reviewContainer.innerHTML = "";
    const sorted = [...reviewDB].sort((a, b) => b.timestamp - a.timestamp);
    const sliced = sorted.slice(0, limit);

    sliced.forEach((r) => {
      const li = document.createElement("li");
      li.innerHTML = `
        <div class="review-box">
          <strong>${maskUsername(r.username)}</strong> <span class="star-icon">⭐</span> ${r.rating || 0}점
          <div class="review-text">${r.review}</div>
          <div class="review-date">🕒 ${formatDate(r.timestamp)}</div>
        </div>
      `;
      reviewContainer.appendChild(li);
    });

    updateReviewCount();

    if (reviewDB.length <= 3) {
      moreBtn.style.display = "none";
    } else {
      moreBtn.style.display = "inline-block";
      moreBtn.textContent = showingAll ? "접기 ❮" : "더보기 ❯";
    }
  }

  stars.forEach((star, index) => {
    star.addEventListener("click", (e) => {
      const rect = star.getBoundingClientRect();
      const clickX = e.clientX - rect.left;
      const isHalf = clickX <= rect.width / 2;
      const value = index + (isHalf ? 0.5 : 1);
      ratingInput.value = value;
      updateStars(value);

      starContainer.classList.add("active");
      setTimeout(() => starContainer.classList.remove("active"), 150);
    });
  });

  updateStars(0);

  document.querySelector(".review-form").addEventListener("submit", function (e) {
    e.preventDefault();

    const input = $("#review");
    const reviewText = input.value.trim();
    const rating = parseFloat(ratingInput.value);

    if (!currentUser.isLoggedIn) {
      alert("로그인 후 작성 가능합니다.");
      return;
    }
    if (!reviewText) {
      alert("후기를 작성해주세요.");
      input.focus();
      return;
    }
    if (reviewText.length > 500) {
      alert("후기는 500자 이하로 작성해주세요.");
      return;
    }
    if (isNaN(rating) || rating <= 0) {
      alert("별점을 선택해주세요.");
      return;
    }

    const maskedName = maskUsername(currentUser.name);
    reviewDB.push({ username: maskedName, review: reviewText, rating, timestamp: new Date() });

    input.value = "";
    ratingInput.value = 0;
    updateStars(0);
    showingAll = false;
    renderReviews(3);
  });

  if (moreBtn) {
    moreBtn.addEventListener("click", () => {
      showingAll = !showingAll;
      renderReviews(showingAll ? 99 : 3);
    });
  }

  // 테스트용 더미 데이터
  reviewDB.push(
      { username: "서유진", review: "방문 추천드려요!", rating: 4, timestamp: new Date("2024-08-22") },
      { username: "마라탕개맛있다", review: "생각보다 괜찮았어요", rating: 3.5, timestamp: new Date("2024-12-10") }
  );

  renderReviews(3);
});

// -------------------- (기존) 방 만들기 프리필 --------------------
document.addEventListener("DOMContentLoaded", () => {
  const btn = $("#btnMakeRoom");
  if (!btn) return;

  btn.addEventListener("click", async (e) => {
    e.preventDefault();

    let attractionId = Number(btn.dataset.attractionId) || null;
    const contentId = btn.dataset.contentId;

    if (!attractionId && contentId) {
      try {
        const res = await fetch(`/api/attractions/content/${contentId}/detail`);
        if (res.ok) {
          const d = await res.json();
          const a = d.attraction || d.base || d;
          attractionId = Number(a?.attractionId) || null;
        }
      } catch (_) {}
    }
    if (!attractionId) {
      alert("관광지 정보를 불러오지 못했습니다.");
      return;
    }

    let emotions = [];
    try {
      if (contentId) {
        const er = await fetch(`/api/attractions/content/${contentId}/emotion-tags`);
        if (er.ok) emotions = await er.json();
      }
    } catch (_) {}
    if (emotions.length === 0) {
      emotions = Array.from(document.querySelectorAll(".emotion-tag, .tag-item, .place-tag-list .tag"))
          .map((el) => (el.textContent || "").replace("#", "").trim())
          .filter(Boolean)
          .slice(0, 3);
    }

    sessionStorage.setItem(
        "room_prefill",
        JSON.stringify({ source: "attraction-detail", attraction: { attractionId }, emotions })
    );

    const redirect = btn.getAttribute("href") || "/companion-rooms/create";
    window.location.href = redirect;
  });
});
