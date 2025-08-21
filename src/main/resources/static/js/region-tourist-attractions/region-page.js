// ==============================
// region-page.js
// ==============================

// --- 페이지/블록 상태 ---
let currentPage = 1;            // 1-base (컨트롤러 pageNo와 동일)
const pageSize = 20;            // 5 x 4 = 20개
const blockSize = 10;           // 페이지네이션 10단위 블록

// --- 지역 선택 상태 ---
const selectedRegionCodes = new Set();

// KR코드 ↔ 표시명
const regionCodeMap = {
  KR11: "서울", KR28: "인천", KR30: "대전", KR27: "대구", KR29: "광주",
  KR26: "부산", KR31: "울산", KR50: "세종", KR41: "경기", KR42: "강원",
  KR43: "충북", KR44: "충남", KR47: "경북", KR48: "경남", KR45: "전북",
  KR46: "전남", KR49: "제주",
};
const areaCodeNameMap = {
  1: "서울", 2: "인천", 3: "대전", 4: "대구", 5: "광주", 6: "부산", 7: "울산", 8: "세종",
  31: "경기", 32: "강원", 33: "충북", 34: "충남", 35: "경북", 36: "경남", 37: "전북", 38: "전남", 39: "제주",
};
function areaCodeToName(areaCode) {
  return areaCodeNameMap[Number(areaCode)] || "";
}

const FALLBACK_IMG = 'data:image/svg+xml;utf8,' +
    encodeURIComponent(`<svg xmlns="http://www.w3.org/2000/svg" width="800" height="600">
  <rect width="100%" height="100%" fill="#f3f4f6"/>
  <g fill="#9ca3af" font-family="system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial" font-size="20">
    <text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle">이미지 없음</text>
  </g>
</svg>`);

const ICON_LOCATION = 'data:image/svg+xml;utf8,' +
    encodeURIComponent(`<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="#6b7280" viewBox="0 0 16 16">
  <path d="M8 0a6 6 0 0 0-6 6c0 4.5 6 10 6 10s6-5.5 6-10a6 6 0 0 0-6-6zm0 8.5A2.5 2.5 0 1 1 8 3.5a2.5 2.5 0 0 1 0 5z"/>
</svg>`);
const ICON_STAR = 'data:image/svg+xml;utf8,' +
    encodeURIComponent(`<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="#f59e0b" viewBox="0 0 16 16">
  <path d="M8 12.1l-4.12 2.28.79-4.6L1.34 6.5l4.62-.67L8 1.6l2.04 4.23 4.62.67-3.33 3.28.79 4.6z"/>
</svg>`);


// --- 서버 호출(지역 다중 + 필터 + 페이지) ---
async function fetchAttractionsByRegions(regionCodes, pageNo = 1) {
  const params = new URLSearchParams();

  // regions 파라미터 반복 전달
  (regionCodes || []).forEach(code => params.append("regions", code));

  // 필터/정렬
  const sort = document.querySelector(".sort-select")?.value || "default";
  const cat1 = document.getElementById("cat1-select")?.value || "";
  const cat2 = document.getElementById("cat2-select")?.value || "";
  const cat3 = document.getElementById("cat3-select")?.value || "";
  const keyword = document.querySelector(".keyword-input")?.value || "";

  if (sort && sort !== "default") params.append("sort", sort);
  if (cat1) params.append("cat1", cat1);
  if (cat2) params.append("cat2", cat2);
  if (cat3) params.append("cat3", cat3);
  if (keyword) params.append("keyword", keyword);

  // 페이지 파라미터 (1-base)
  params.append("pageNo", pageNo);
  params.append("numOfRows", pageSize);

  const res = await fetch(`/api/attractions/detail-regions?${params.toString()}`, {
    headers: { "Accept": "application/json" }
  });
  if (!res.ok) throw new Error("failed to fetch attractions");
  return res.json(); // { items,totalElements,totalPages,page,size } 등
}

// --- 렌더: 카드 ---
function renderAttractionCards(list) {
  const container = document.querySelector(".tour-card-list");
  if (!container) return;
  container.innerHTML = "";

  if (!list || list.length === 0) {
    container.innerHTML = `<div class="empty">선택한 조건의 관광지가 없습니다.</div>`;
    return;
  }

  list.forEach(item => {
    const regionName = areaCodeToName(item.areaCode);
    const pick = (v) => (typeof v === 'string' && v.trim().length > 0) ? v : null;
    const initialSrc = pick(item.firstImage) || pick(item.firstImage2) || FALLBACK_IMG;

    const card = document.createElement("div");
    card.className = "tour-card";
    card.dataset.region = regionName;

    card.innerHTML = `
      <div class="card-image-wrapper">
        <img class="card-image" alt="${item.title || ""}" loading="lazy" decoding="async">
      </div>
      <div class="card-content">
        <div class="card-meta">
          <span class="category">${regionName}${item.addr1 ? ` · ${item.addr1}` : ""}</span>
        </div>
        <h3 class="card-title">${item.title || ""}</h3>
      </div>
    `;

    const img = card.querySelector(".card-image");
    img.src = initialSrc;
    img.onerror = () => { img.onerror = null; img.src = FALLBACK_IMG; }; // 루프 방지

    // 카드 생성 후
    card.addEventListener("click", () => {
      location.href = `/templates/recommand-tourist-attractions-detail/detail-page.html?contentId=${item.contentId}`;
    });


    container.appendChild(card);
  });
}


// --- 렌더: 페이지네이션 (10단위 블록 + 점프) ---
function renderPagination(totalPages, pageNow1Base) {
  const wrap = document.querySelector(".pagination");
  if (!wrap) return;

  wrap.innerHTML = "";
  if (!totalPages || totalPages <= 1) return;

  const makeBtn = (label, disabled, goTo) => {
    const b = document.createElement("button");
    b.textContent = label;
    if (disabled) b.disabled = true;
    if (!disabled && typeof goTo === "number") {
      b.addEventListener("click", () => {
        currentPage = goTo;
        updateTourCardsVisibility();
      });
    }
    return b;
  };


  const page = Math.max(1, pageNow1Base);
  const blockIdx = Math.floor((page - 1) / blockSize);
  const start = blockIdx * blockSize + 1;
  const end = Math.min(totalPages, start + blockSize - 1);

  // 점프/이동 버튼
  wrap.appendChild(makeBtn("«", page === 1, 1));
  wrap.appendChild(makeBtn("‹", page === 1, Math.max(1, page - 1)));
  wrap.appendChild(makeBtn("⟪", start === 1, Math.max(1, start - blockSize)));

  // 숫자 버튼
  for (let p = start; p <= end; p++) {
    const btn = makeBtn(String(p), false, p);
    if (p === page) btn.classList.add("active");
    wrap.appendChild(btn);
  }

  wrap.appendChild(makeBtn("⟫", end === totalPages, Math.min(totalPages, end + 1)));
  wrap.appendChild(makeBtn("›", page === totalPages, Math.min(totalPages, page + 1)));
  wrap.appendChild(makeBtn("»", page === totalPages, totalPages));
}

// --- 메인: 카드/페이지 갱신 ---
function updateTourCardsVisibility() {
  const container = document.querySelector(".tour-card-list");
  const noSelection = document.getElementById("no-selection");

  const selectedCodes = Array.from(selectedRegionCodes);
  if (selectedCodes.length === 0) {
    if (noSelection) noSelection.style.display = "block";
    if (container) container.innerHTML = "";
    const p = document.querySelector(".pagination");
    if (p) p.innerHTML = "";
    return;
  }
  if (noSelection) noSelection.style.display = "none";

  fetchAttractionsByRegions(selectedCodes, currentPage)
      .then(resp => {
        const items = resp.items ?? resp.list ?? [];
        const totalPages =
            resp.totalPages ??
            Math.max(1, Math.ceil((resp.totalElements || 0) / (resp.size || pageSize)));
        const page1 = (typeof resp.page === "number") ? resp.page + 1 : currentPage;

        renderAttractionCards(items);
        renderPagination(totalPages, page1);
      })
      .catch(err => {
        if (myReq !== lastReqId) return;
        console.error(err);
        if (container) container.innerHTML = `<div class="error">목록을 불러오지 못했습니다.</div>`;
      });
}

// --- 지역 UI 갱신 & 토글 ---
function updateUI() {
  const buttons = document.querySelectorAll(".r-button");
  const svgRoot = document.querySelector(".map-svg");

  // 버튼 강조
  buttons.forEach(btn => {
    const code = btn.dataset.region;
    btn.classList.toggle("selected", selectedRegionCodes.has(code));
  });

  // 지도 path/text 강조
  svgRoot?.querySelectorAll("path")?.forEach(path => {
    path.classList.toggle("selected", selectedRegionCodes.has(path.id));
  });
  svgRoot?.querySelectorAll("text")?.forEach(text => {
    const regionName = text.getAttribute("data-region");
    const code = Object.keys(regionCodeMap).find(k => regionCodeMap[k] === regionName);
    if (code) text.classList.toggle("selected", selectedRegionCodes.has(code));
  });

  // 선택 태그
  const selectedContainer = document.getElementById("selected-regions");
  if (selectedContainer) {
    selectedContainer.innerHTML = "";
    selectedRegionCodes.forEach(code => {
      const name = regionCodeMap[code];
      if (name) {
        const tag = document.createElement("div");
        tag.className = "tag";
        tag.textContent = name;
        selectedContainer.appendChild(tag);
      }
    });
  }

  currentPage = 1; // 지역 바뀌면 첫 페이지부터
  updateTourCardsVisibility();
}
function toggleRegion(code) {
  if (!code) return;
  if (selectedRegionCodes.has(code)) selectedRegionCodes.delete(code);
  else selectedRegionCodes.add(code);
  updateUI();
}

// --- 초기 바인딩 ---
document.addEventListener("DOMContentLoaded", () => {
  const buttons = document.querySelectorAll(".r-button");
  const svgRoot = document.querySelector(".map-svg");

  // 버튼/지도 클릭
  buttons.forEach(btn => btn.addEventListener("click", () => toggleRegion(btn.dataset.region)));
  svgRoot?.querySelectorAll("path").forEach(p => p.addEventListener("click", () => toggleRegion(p.id)));
  svgRoot?.querySelectorAll("text").forEach(t => {
    const rn = t.getAttribute("data-region");
    const code = Object.keys(regionCodeMap).find(k => regionCodeMap[k] === rn);
    if (code) t.addEventListener("click", () => toggleRegion(code));
  });

  // 정렬/검색/카테고리 변경
  document.querySelectorAll(".sort-select").forEach(sel =>
      sel.addEventListener("change", () => { currentPage = 1; updateTourCardsVisibility(); })
  );
  document.querySelector(".keyword-input")?.addEventListener("keydown", e => {
    if (e.key === "Enter") { currentPage = 1; updateTourCardsVisibility(); }
  });
  ["cat1-select","cat2-select","cat3-select"].forEach(id => {
    document.getElementById(id)?.addEventListener("change", () => { currentPage = 1; updateTourCardsVisibility(); });
  });

  // 초기 렌더
  updateTourCardsVisibility();

  // 리뷰 슬라이더
  populateReviewSlider();
});

// --- 단일 지역 드롭다운(옵션)
function handleRegionChange(value) {
  document.querySelectorAll(".tour-card").forEach(card => {
    card.style.display = value && card.dataset.region === value ? "block" : "none";
  });
  const noSel = document.getElementById("no-selection");
  if (noSel) noSel.style.display = value ? "none" : "block";
}

// --- 외부에서 호출할 정렬 핸들러(옵션)
function handleSortChange(value) {
  const first = document.querySelector(".sort-select");
  if (first) first.value = value;
  currentPage = 1;
  updateTourCardsVisibility();
}

// --- (데모) 리뷰 슬라이더: 정적 이미지 경로만 수정 ---
const places = [
  { name: "사려니숲길", image: "/image/region-tourist-attractions/사려니숲길.png",
    reviews: [{ content: "우리 여름에 흙길 다른 관광하면서 돌아보았는데...", user: "oh******", rating: "4.80" }] },
  { name: "용두암", image: "/image/region-tourist-attractions/사려니숲길.png",
    reviews: [{ content: "파도가 부서지는 장면이 너무 멋져요.", user: "yo******", rating: "4.90" }] },
];
function populateReviewSlider() {
  const slider = document.querySelector(".slider");
  const indicator = document.querySelector(".slider-button");
  if (!slider || !indicator) return;

  slider.innerHTML = "";
  indicator.innerHTML = "";

  places.forEach(p => {
    const r = p.reviews[0]; if (!r) return;
    const slide = document.createElement("div");
    slide.className = "tour-card-review";
    slide.innerHTML = `
      <div class="review-image-section">
        <img class="review-photo" src="${p.image}" alt="${p.name}">
        <div class="place-name">
          <img class="location-icon" src="/image/region-tourist-attractions/location.png">
          ${p.name}
        </div>
      </div>
      <div class="review-content">
        <p class="review-content-text">“${r.content}”</p>
        <div class="review-content-rate">
          <img class="star-icon" src="/image/region-tourist-attractions/star.png">
          <div class="review-rate-text">${r.rating}</div>
          <div class="review-divider"></div>
          <div class="review-user">${r.user}님의 후기</div>
        </div>
      </div>`;
    slider.appendChild(slide);
  });

  // 이미 프로젝트에 slick 초기화가 있으면 그걸 사용 (중복 초기화 방지)
  if (typeof $ !== "undefined" && !$(slider).hasClass("slick-initialized")) {
    $(slider).slick({
      slidesToShow: 1,
      slidesToScroll: 1,
      infinite: true,
      dots: true,
      arrows: false,
      autoplay: true,
      autoplaySpeed: 4000,
      appendDots: $('.slider-button'),
      customPaging: (_, i) => `<button type="button">${i + 1}</button>`,
      dotsClass: 'slick-dots',
    });
  }
}
