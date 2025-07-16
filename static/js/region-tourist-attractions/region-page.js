// 전역 선언 (지역 선택/필터용)
const selectedRegionCodes = new Set();
const regionCodeMap = {
  "KR11": "서울",  "KR28": "인천",  "KR30": "대전",  "KR27": "대구",
  "KR29": "광주",  "KR26": "부산",  "KR31": "울산",  "KR50": "세종",
  "KR41": "경기",  "KR42": "강원",  "KR43": "충북",  "KR44": "충남",
  "KR47": "경북",  "KR48": "경남",  "KR45": "전북",  "KR46": "전남",  "KR49": "제주"
};

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

function updateTourCardsVisibility() {
  const allCards = document.querySelectorAll(".tour-card");
  const container = document.querySelector(".tour-card-list");
  const sortSelect = document.querySelector(".sort-select");
  const sortValue = sortSelect?.value || "default";

  const selectedRegionNames = [...selectedRegionCodes].map(code => regionCodeMap[code]);

  const noSelection = document.getElementById("no-selection");
  if (selectedRegionNames.length === 0) {
    if (noSelection) noSelection.style.display = "block";
    allCards.forEach(card => card.style.display = "none");
    return;
  } else {
    if (noSelection) noSelection.style.display = "none";
  }

  // 선택된 지역 카드만 필터링
  let visibleCards = Array.from(allCards).filter(card =>
    selectedRegionNames.includes(card.dataset.region)
  );

  // 정렬
  if (sortValue === "rating") {
    visibleCards.sort((a, b) => {
      const rA = parseFloat(a.dataset.rating || 0);
      const rB = parseFloat(b.dataset.rating || 0);
      return rB - rA;
    });
  } else if (sortValue === "portfolio") {
    visibleCards.sort((a, b) => {
      const nameA = a.querySelector(".card-title")?.textContent.trim() || "";
      const nameB = b.querySelector(".card-title")?.textContent.trim() || "";
      return nameA.localeCompare(nameB, "ko");
    });
  }

  // 전체 숨기고, 정렬된 카드만 다시 append
  allCards.forEach(card => card.style.display = "none");
  visibleCards.forEach(card => {
    card.style.display = "inline-block";
    container.appendChild(card);
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
  updateTourCardsVisibility();
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


