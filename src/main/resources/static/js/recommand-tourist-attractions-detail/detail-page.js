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
  if (!d) {
    const params = new URLSearchParams(location.search);
    const cidRaw =
        params.get("contentId") ||
        (document.getElementById("btnMakeRoom")?.dataset.contentId ?? "");
    const contentId = /^\d+$/.test(cidRaw) ? cidRaw : "";   // ← 숫자만 허용
    if (!contentId) {
      console.warn("잘못된 contentId:", cidRaw);
      return;
    }

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

  const btnWrapper = document.querySelector(".like-btn");
  const likeSpan = btnWrapper?.querySelector("span");
  const attractionId = btnWrapper?.dataset.attractionId;

  if (btnWrapper && likeSpan && attractionId) {
    // 초기 상태 가져오기
    try {
      const r = await fetch(`/api/likes/${attractionId}`, { headers: { Accept: "application/json" }});
      if (r.ok) {
        const { liked } = await r.json();
        btnWrapper.classList.toggle("liked", liked);
        likeSpan.textContent = liked ? "♥" : "♡";
      }
    } catch {
      likeSpan.textContent = "♡";
    }

    // 클릭 이벤트
    btnWrapper.addEventListener("click", async () => {
      const isLikedNow = btnWrapper.classList.contains("liked");
      const method = isLikedNow ? "DELETE" : "POST";
      const res = await fetch(`/api/likes/${attractionId}`, { method });
      if (res.ok) {
        btnWrapper.classList.toggle("liked", !isLikedNow);
        likeSpan.textContent = !isLikedNow ? "♥" : "♡";
      } else if (res.status === 401) {
        alert("로그인이 필요합니다.");
      }
    });
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


// -------------------- (기존) 방 만들기 프리필 --------------------
// document.addEventListener("DOMContentLoaded", () => {
//   const btn = $("#btnMakeRoom");
//   if (!btn) return;
//
//   btn.addEventListener("click", async (e) => {
//     e.preventDefault();
//
//     let attractionId = Number(btn.dataset.attractionId) || null;
//     const contentId = btn.dataset.contentId;
//
//     if (!attractionId && contentId) {
//       try {
//         const res = await fetch(`/api/attractions/content/${contentId}/detail`);
//         if (res.ok) {
//           const d = await res.json();
//           const a = d.attraction || d.base || d;
//           attractionId = Number(a?.attractionId) || null;
//         }
//       } catch (_) {}
//     }
//     if (!attractionId) {
//       alert("관광지 정보를 불러오지 못했습니다.");
//       return;
//     }
//
//     let emotions = [];
//     try {
//       if (contentId) {
//         const er = await fetch(`/api/attractions/content/${contentId}/emotion-tags`);
//         if (er.ok) emotions = await er.json();
//       }
//     } catch (_) {}
//     if (emotions.length === 0) {
//       emotions = Array.from(document.querySelectorAll(".emotion-tag, .tag-item, .place-tag-list .tag"))
//           .map((el) => (el.textContent || "").replace("#", "").trim())
//           .filter(Boolean)
//           .slice(0, 3);
//     }
//
//     sessionStorage.setItem(
//         "room_prefill",
//         JSON.stringify({ source: "attraction-detail", attraction: { attractionId }, emotions })
//     );
//
//     const redirect = btn.getAttribute("href") || "/companion-rooms/create";
//     window.location.href = redirect;
//   });
// });

// document.addEventListener("DOMContentLoaded", () => {
//   const btn = document.getElementById("btnMakeRoom");
//   if (!btn) return;
//
//   btn.addEventListener("click", async (e) => {
//     e.preventDefault();
//
//     let attractionId = Number(btn.dataset.attractionId) || null;
//     const contentId  = btn.dataset.contentId ? Number(btn.dataset.contentId) : null;
//
//     let detail = null, base = null;
//     try {
//       if (contentId) {
//         const r = await fetch(`/api/attractions/content/${contentId}/detail`, { headers:{Accept:"application/json"} });
//         if (r.ok) detail = await r.json(); // { title, image, addr, ... }
//       }
//     } catch (_) {}
//
//     try {
//       const r2 = await fetch(`/api/attractions/content/${contentId}/detail`, { headers:{Accept:"application/json"} });
//       if (r2.ok) {
//         const d = await r2.json();
//         base = d.attraction || d.base || null; // 지금 구조상 대부분 null일 수 있음
//         attractionId = attractionId || Number(base?.attractionId) || null;
//       }
//     } catch (_) {}
//
//     // 👉 address(통합 주소) 확보
//     const address = (detail?.addr || "").trim();
//
//     // addr1/addr2가 없을 때 address를 그대로 써도 되고, 간단히 두 토막으로 나눠도 됩니다.
//     let addr1 = base?.addr1 || "";
//     let addr2 = base?.addr2 || "";
//     if (!addr1 && !addr2 && address) {
//       const m = address.match(/^(\S+\s*\S*)(?:\s+(.+))?$/); // 대충 앞 1~2토막 + 나머지
//       addr1 = m?.[1] || "";
//       addr2 = m?.[2] || "";
//     }
//
//     // 감정 태그 그대로
//     let emotions = [];
//     try {
//       if (contentId) {
//         const er = await fetch(`/api/attractions/content/${contentId}/emotion-tags`, { headers:{Accept:"application/json"} });
//         if (er.ok) emotions = await er.json();
//       }
//     } catch (_) {}
//     if (!Array.isArray(emotions) || emotions.length === 0) {
//       emotions = Array.from(document.querySelectorAll(".place-tag .tag-item, .emotion-tag, .place-tag-list .tag"))
//           .map(el => (el.textContent || "").replace(/^#/, "").trim())
//           .filter(Boolean)
//           .slice(0, 3);
//     }
//
//     const attrForPrefill = {
//       attractionId,
//       contentId,
//       title: base?.title || detail?.title || document.querySelector(".place-name")?.textContent || "",
//       firstImage: base?.firstImage || detail?.image || "",
//       address,        // 통합 주소 (NEW)
//       addr1,          // 주소 앞부분
//       addr2           // 주소 뒷부분
//     };
//
//     const payload = { source: "attraction-detail", attraction: attrForPrefill, emotions };
//     sessionStorage.setItem("room_prefill", JSON.stringify(payload));
//     localStorage.setItem("room_prefill", JSON.stringify(payload));
//
//     window.location.href = btn.getAttribute("href") || "/companion-rooms/create";
//   });
// });

// 1) 문자열 배열로 정규화
function normalizeEmotionNames(arr) {
  if (!Array.isArray(arr)) return [];
  return [...new Set(arr.map(e => {
    if (typeof e === 'string') return e.trim();
    if (e && typeof e === 'object')
      return String(e.tagName || e.name || e.text || '').trim();
    return '';
  }).filter(Boolean))];
}

document.addEventListener("DOMContentLoaded", () => {
  const btn = document.getElementById("btnMakeRoom");
  if (!btn) return;

  // 감정명 정규화(+중복 제거) 유틸
  const normalizeEmotionNames = (list) => {
    const seen = new Set();
    const out  = [];
    (list || []).forEach((raw) => {
      const s = String(raw || "")
          .replace(/^#/, "")   // 앞의 # 제거
          .replace(/\s+/g, "") // 내부 공백 제거
          .trim();
      if (!s || seen.has(s)) return;
      seen.add(s);
      out.push(s);
    });
    return out;
  };

  btn.addEventListener("click", async (e) => {
    e.preventDefault();

    let attractionId = Number(btn.dataset.attractionId) || null;
    const contentId  = btn.dataset.contentId ? Number(btn.dataset.contentId) : null;

    let detail = null, base = null;

    // 상세(통합) 정보
    try {
      if (contentId) {
        const r = await fetch(`/api/attractions/content/${contentId}/detail`, { headers:{Accept:"application/json"} });
        if (r.ok) detail = await r.json(); // { title,image,addr,tel,... }
      }
    } catch (_) {}

    // (있다면) 엔티티 기반 정보
    try {
      if (contentId) {
        const r2 = await fetch(`/api/attractions/content/${contentId}/detail`, { headers:{Accept:"application/json"} });
        if (r2.ok) {
          const d = await r2.json();
          base = d.attraction || d.base || null;
          attractionId = attractionId || Number(base?.attractionId) || null;
        }
      }
    } catch (_) {}

    // 주소 보강
    const address = (detail?.addr || "").trim();
    let addr1 = base?.addr1 || "", addr2 = base?.addr2 || "";
    if (!addr1 && !addr2 && address) {
      // 앞부분(광역/시군구 추정) + 나머지
      const m = address.match(/^(\S+\s*\S*)(?:\s+(.+))?$/);
      addr1 = m?.[1] || "";
      addr2 = m?.[2] || "";
    }

    // 감정 태그 수집(최대 3개)
    let emotions = [];
    try {
      if (contentId) {
        const er = await fetch(`/api/attractions/content/${contentId}/emotion-tags`, { headers:{Accept:"application/json"} });
        if (er.ok) emotions = await er.json();  // 예: ["힐링","감성",...]
      }
    } catch (_) {}

    // API가 비어있으면 화면 태그에서 수집
    if (!Array.isArray(emotions) || emotions.length === 0) {
      emotions = Array.from(document.querySelectorAll(".place-tag .tag-item, .emotion-tag, .place-tag-list .tag"))
          .map(el => (el.textContent || ""))
          .filter(Boolean);
    }
    const emotionNames = normalizeEmotionNames(emotions).slice(0, 3);

    // 프리필 페이로드
    const attrForPrefill = {
      attractionId,
      contentId,
      title: base?.title || detail?.title || document.querySelector(".place-name")?.textContent || "",
      firstImage: base?.firstImage || detail?.image || "",
      address,  // 통합 주소
      addr1,
      addr2
    };

    const TEST_TTL_MS = 60 * 10 * 1000;
    const payload = {
      source: "attraction-detail", // 상세에서 온 프리필임을 표시
      ts: Date.now(),              // 타임스탬프
      exp: Date.now() + TEST_TTL_MS,
      attraction: attrForPrefill,
      contentId,                   // 선택 페이지에서 바로 쓰라고 탑레벨에도
      emotions: emotionNames       // 정규화된 감정명(최대 3개)
    };

    // 세션에만 저장 + 마커 남기기
    sessionStorage.setItem("room_prefill", JSON.stringify(payload));
    sessionStorage.setItem("prefill_from_detail", "1");
    localStorage.removeItem("room_prefill");

    // 혹시 남아있던 과거값은 정리
    localStorage.removeItem("room_prefill");

    // 이동
    window.location.href = btn.getAttribute("href") || "/companion-rooms/create";
  });
});