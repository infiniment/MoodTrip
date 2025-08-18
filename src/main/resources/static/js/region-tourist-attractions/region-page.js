// 전역 선언 (지역 선택/필터용)
const selectedRegionCodes = new Set();
const regionCodeMap = {
  "KR11": "서울",  "KR28": "인천",  "KR30": "대전",  "KR27": "대구",
  "KR29": "광주",  "KR26": "부산",  "KR31": "울산",  "KR50": "세종",
  "KR41": "경기",  "KR42": "강원",  "KR43": "충북",  "KR44": "충남",
  "KR47": "경북",  "KR48": "경남",  "KR45": "전북",  "KR46": "전남",  "KR49": "제주"
};
const areaCodeNameMap = {
  1: "서울", 2: "인천", 3: "대전", 4: "대구", 5: "광주", 6: "부산", 7: "울산", 8: "세종",
  31: "경기", 32: "강원", 33: "충북", 34: "충남", 35: "경북", 36: "경남", 37: "전북", 38: "전남", 39: "제주"
};
function areaCodeToName(areaCode) {
  return areaCodeNameMap[Number(areaCode)] || "";
}

document.addEventListener("DOMContentLoaded", () => {
  const buttons = document.querySelectorAll(".r-button");
  const svgRoot = document.querySelector(".map-svg");
  const sortSelect = document.querySelector(".sort-select");

  // ✅ 지역 강조 및 UI 갱신
  function updateUI() {
    // 버튼 강조
    buttons.forEach((btn) => {
      const code = btn.dataset.region;
      btn.classList.toggle("selected", selectedRegionCodes.has(code));
    });

    // path 강조
    svgRoot.querySelectorAll("path").forEach((path) => {
      const code = path.id;
      path.classList.toggle("selected", selectedRegionCodes.has(code));
    });

    // text 강조
    svgRoot.querySelectorAll("text").forEach((text) => {
      const regionName = text.getAttribute("data-region");
      const matchingCode = Object.keys(regionCodeMap).find(
          (key) => regionCodeMap[key] === regionName
      );
      text.classList.toggle("selected", selectedRegionCodes.has(matchingCode));
    });

    // 선택된 지역 리스트 표시
    const selectedContainer = document.getElementById("selected-regions");
    if (selectedContainer) {
      selectedContainer.innerHTML = "";
      selectedRegionCodes.forEach((code) => {
        const name = regionCodeMap[code];
        if (name) {
          const tag = document.createElement("div");
          tag.className = "tag";
          tag.textContent = name;
          selectedContainer.appendChild(tag);
        }
      });
    }

    updateTourCardsVisibility(); // ← 이거 꼭 필요!
  }

  // ✅ 지역 선택 토글
  function toggleRegion(regionCode) {
    if (!regionCode) return;

    if (selectedRegionCodes.has(regionCode)) {
      selectedRegionCodes.delete(regionCode);
    } else {
      selectedRegionCodes.add(regionCode);
    }

    updateUI();
  }

  // ✅ 버튼 클릭
  buttons.forEach((btn) => {
    btn.addEventListener("click", () => {
      toggleRegion(btn.dataset.region);
    });
  });

  // ✅ path 클릭
  svgRoot.querySelectorAll("path").forEach((path) => {
    const code = path.id;
    if (code) {
      path.addEventListener("click", () => {
        toggleRegion(code);
      });
    }
  });

  // ✅ 텍스트 클릭
  svgRoot.querySelectorAll("text").forEach((text) => {
    const regionName = text.getAttribute("data-region");
    const code = Object.keys(regionCodeMap).find(
        (key) => regionCodeMap[key] === regionName
    );
    if (code) {
      text.addEventListener("click", () => {
        toggleRegion(code);
      });
    }
  });

  // ✅ 초기 카드 숨김 처리
  updateTourCardsVisibility();
  populateReviewSlider();
});

// [ADDED] 서버에서 지역별 관광지 목록을 가져오는 함수
async function fetchAttractionsByRegions(regionCodes, sortValue) {
  const params = new URLSearchParams();
  (regionCodes || []).forEach(c => params.append("regions", c));
  if (sortValue && sortValue !== "default") params.append("sort", sortValue);

  const res = await fetch(`/api/attractions/detail-regions?${params.toString()}`, {
    headers: { "Accept": "application/json" }
  });
  if (!res.ok) throw new Error("failed to fetch attractions");
  return res.json(); // -> Array<AttractionResponse>
}

// [ADDED] 받아온 목록을 카드로 렌더링 (상세 이동은 아직 제외)
function renderAttractionCards(list) {
  const container = document.querySelector(".tour-card-list");
  container.innerHTML = "";
  if (!list?.length) { container.innerHTML = `<div class="empty">선택한 지역의 관광지가 없습니다.</div>`; return; }

  list.forEach(item => {
    const regionName = areaCodeToName(item.areaCode);
    const card = document.createElement("div");
    card.className = "tour-card";
    card.dataset.region = regionName;
    card.innerHTML = `
      <div class="card-image-wrapper">
        <img class="card-image" src="${item.firstImage || '/static/image/region-tourist-attractions/사려니숲길.png'}" alt="${item.title || ''}">
      </div>
      <div class="card-content">
        <div class="card-meta"><span class="category">${regionName}</span></div>
        <h3 class="card-title">${item.title || ''}</h3>
      </div>
    `;
    container.appendChild(card);
  });
}

// ✅ 선택/정렬에 따라 카드 표시 갱신
function updateTourCardsVisibility() {
  // [CHANGED] 기존: DOM에 이미 있는 .tour-card들을 필터/정렬
  // → 변경: 선택된 지역으로 서버 호출 후 목록 렌더링

  const container = document.querySelector(".tour-card-list");
  const sortSelect = document.querySelector(".sort-select");
  const sortValue = sortSelect?.value || "default";

  // 선택된 지역 코드들(KR11 등)을 한글명으로 바꾸던 기존 로직 대신, 서버는 코드 그대로 받도록 함
  const selectedCodes = [...selectedRegionCodes];

  const noSelection = document.getElementById("no-selection");
  if (selectedCodes.length === 0) {
    if (noSelection) noSelection.style.display = "block";
    if (container) container.innerHTML = "";
    return;
  } else {
    if (noSelection) noSelection.style.display = "none";
  }

  // 서버 호출 → 렌더
  fetchAttractionsByRegions(selectedCodes, sortValue)
      .then(list => {
        renderAttractionCards(list);
      })
      .catch(err => {
        console.error(err);
        if (container) {
          container.innerHTML = `<div class="error">목록을 불러오지 못했습니다.</div>`;
        }
      });
}

// ✅ 드롭다운 전용 핸들러 (별도로 선택 시 사용)
function handleRegionChange(value) {
  const cards = document.querySelectorAll(".tour-card");

  cards.forEach(card => {
    const region = card.dataset.region;
    card.style.display = value && region === value ? "block" : "none";
  });

  const noSelection = document.getElementById("no-selection");
  if (noSelection) {
    noSelection.style.display = value ? "none" : "block";
  }
}
document.querySelector(".sort-select").addEventListener("change", () => {
  updateTourCardsVisibility(); // [UNCHANGED] 정렬 바뀌면 새로 불러오기
});

const places = [
  {
    name: "사려니숲길",
    image: "/static/image/region-tourist-attractions/사려니숲길.png",
    reviews: [
      {
        content: "우리 여름에 흙길 다른 관광하면서 돌아보았는데...",
        user: "oh******",
        rating: "4.80"
      },
      {
        content: "자연이 좋고 가족과 함께 산책하기 너무 좋아요!",
        user: "ja******",
        rating: "5.00"
      }
    ]
  },
  {
    name: "용두암",
    image: "/static/image/region-tourist-attractions/사려니숲길.png",
    reviews: [
      {
        content: "파도가 부서지는 장면이 너무 멋져요.",
        user: "yo******",
        rating: "4.90"
      },
      {
        content: "사진 찍기 딱 좋은 곳이었어요.",
        user: "ph******",
        rating: "5.00"
      }
    ]
  },
  {
    name: "천지연폭포",
    image: "/static/image/region-tourist-attractions/사려니숲길.png",
    reviews: [
      {
        content: "시원하고 웅장한 폭포 소리가 아직도 귀에 맴도네요.",
        user: "cj******",
        rating: "4.70"
      }
    ]
  },
  {
    name: "한라산",
    image: "/static/image/region-tourist-attractions/사려니숲길.png",
    reviews: [
      {
        content: "눈 덮인 풍경이 예술입니다.",
        user: "hl******",
        rating: "5.00"
      },
      {
        content: "등산로가 잘 정비되어 있어요!",
        user: "an******",
        rating: "4.85"
      }
    ]
  },
  {
    name: "협재해수욕장",
    image: "/static/image/region-tourist-attractions/사려니숲길.png",
    reviews: [
      {
        content: "물이 맑고, 물놀이 하기 좋아요.",
        user: "hy******",
        rating: "4.90"
      },
      {
        content: "일몰이 환상적이에요.",
        user: "su******",
        rating: "5.00"
      }
    ]
  }
];

function populateReviewSlider() {
  const slider = document.querySelector(".slider");
  const indicatorContainer = document.querySelector(".slider-button");
  if (!slider || !indicatorContainer) return;

  // 기존 슬라이드/버튼 초기화
  slider.innerHTML = "";
  indicatorContainer.innerHTML = "";

  places.forEach((place) => {
    const review = place.reviews[0];
    if (!review) return;

    // 슬라이드 생성
    const slide = document.createElement("div");
    slide.className = "tour-card-review";
    slide.innerHTML = `
      <div class="review-image-section">
        <img class="review-photo" src="${place.image}" alt="${place.name}">
        <div class="place-name">
          <img class="location-icon" src="/static/image/region-tourist-attractions/location.png">
          ${place.name}
        </div>
      </div>
      <div class="review-content">
        <p class="review-content-text">“${review.content}”</p>
        <div class="review-content-rate">
          <img class="star-icon" src="/static/image/region-tourist-attractions/star.png">
          <div class="review-rate-text">${review.rating}</div>
          <div class="review-divider"></div>
          <div class="review-user">${review.user}님의 후기</div>
        </div>
      </div>
    `;
    slider.appendChild(slide);
  });

  // 슬라이더 활성화 (단 한 번만!)
  if (!$(slider).hasClass('slick-initialized')) {
    $(slider).slick({
      slidesToShow: 1,
      slidesToScroll: 1,
      infinite: true,
      dots: true,
      arrows: false,
      autoplay: true,
      autoplaySpeed: 4000,
      appendDots: $('.slider-button'),
      customPaging: function (_, i) {
        return `<button type="button">${i + 1}</button>`;
      },
      dotsClass: 'slick-dots',
    });
  }
}
