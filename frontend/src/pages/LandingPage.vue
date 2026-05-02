<template>
  <main class="landing-page">
    <section class="hero">
      <nav class="hero-nav" aria-label="Boontory landing navigation">
        <RouterLink class="brand" to="/landing" aria-label="Boontory home">
          <span class="brand-mark">B</span>
          <span>Boontory</span>
        </RouterLink>
        <RouterLink class="nav-cta" to="/">Open app</RouterLink>
      </nav>

      <div class="hero-grid">
        <div class="hero-copy">
          <p class="eyebrow">Personal Book Library · Mobile-first ISBN scanner</p>
          <h1>Scan your shelves. Know your books.</h1>
          <p class="subheadline">
            Boontory helps you catalog your personal book collection in minutes: scan ISBN
            barcodes with your phone, fetch book details from Open Library, and track shelves,
            reading status, ratings, notes, and read dates in one simple library.
          </p>
          <div class="hero-actions">
            <RouterLink class="button button--primary" to="/scan">Start scanning</RouterLink>
            <RouterLink class="button button--secondary" to="/library/new">Add manually</RouterLink>
          </div>
          <p class="microcopy">No spreadsheet setup. No social feed. Just your books, organized.</p>
        </div>

        <div class="product-card" aria-label="Boontory product preview">
          <div class="phone-shell">
            <div class="phone-topbar">
              <span></span>
              <span></span>
              <span></span>
            </div>
            <div class="scanner-panel">
              <div class="scan-frame">
                <span class="scan-line"></span>
              </div>
              <p>ISBN scan ready</p>
            </div>
            <div class="book-preview">
              <div class="cover">DUNE</div>
              <div>
                <strong>Dune</strong>
                <span>Frank Herbert · Read · ★★★★★</span>
              </div>
            </div>
          </div>
          <div class="floating-card floating-card--top">Open Library metadata</div>
          <div class="floating-card floating-card--bottom">Shelves · Notes · Ratings</div>
        </div>
      </div>
    </section>

    <section class="benefits" aria-labelledby="benefits-heading">
      <div class="section-heading">
        <p class="eyebrow">Why Boontory</p>
        <h2 id="benefits-heading">Built for real shelves, not noisy feeds.</h2>
      </div>

      <div class="benefit-grid">
        <article class="benefit-card">
          <span class="benefit-icon">▥</span>
          <h3>Add books faster with ISBN scanning</h3>
          <p>
            Point your phone camera at an ISBN-13 barcode and Boontory looks up title,
            author, cover, and metadata through Open Library. Manual editing stays available
            for rare or older books.
          </p>
        </article>

        <article class="benefit-card">
          <span class="benefit-icon">▦</span>
          <h3>Match your digital library to your physical shelves</h3>
          <p>
            Create shelves for rooms, bookcases, genres, or collections. Search and filter
            what you own before you buy another duplicate copy.
          </p>
        </article>

        <article class="benefit-card">
          <span class="benefit-icon">✦</span>
          <h3>Remember what each book meant</h3>
          <p>
            Keep private ratings, notes, reading status, and read dates together so your
            library becomes useful memory, not just inventory.
          </p>
        </article>
      </div>
    </section>

    <section class="workflow" aria-labelledby="workflow-heading">
      <div>
        <p class="eyebrow">How it works</p>
        <h2 id="workflow-heading">From barcode to organized collection.</h2>
      </div>
      <ol class="steps">
        <li><strong>Scan</strong><span>Use your phone camera to scan an ISBN-13 barcode.</span></li>
        <li><strong>Enrich</strong><span>Fetch metadata from Open Library or edit it manually.</span></li>
        <li><strong>Organize</strong><span>Assign shelves, status, ratings, notes, and read dates.</span></li>
      </ol>
    </section>

    <section class="final-cta">
      <p class="eyebrow">Private library-first</p>
      <h2>Your physical books, finally searchable.</h2>
      <p>Boontory is personal-library-first: no public reviews marketplace, no social pressure, no spreadsheet maintenance.</p>
      <RouterLink class="button button--primary" to="/">Open Boontory</RouterLink>
    </section>
  </main>
</template>

<style scoped>
.landing-page {
  width: 100%;
  min-height: 100vh;
  overflow-x: hidden;
}

.hero {
  max-width: 1180px;
  margin: 0 auto;
  padding: 28px 20px 72px;
}

.hero-nav,
.hero-grid,
.benefits,
.workflow,
.final-cta {
  position: relative;
  z-index: 1;
}

.hero-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 72px;
}

.brand,
.nav-cta,
.button {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-weight: 800;
}

.brand-mark {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border-radius: 12px;
  background: var(--text);
  color: #fff7ed;
  box-shadow: var(--shadow);
}

.nav-cta {
  padding: 10px 16px;
  border: 1px solid var(--border);
  border-radius: 999px;
  background: var(--surface);
  color: var(--accent-strong);
}

.hero-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(320px, 0.95fr);
  gap: 54px;
  align-items: center;
}

.hero-copy,
.product-card,
.benefit-card,
.workflow,
.final-cta {
  min-width: 0;
}

.hero-copy h1,
.hero-copy p,
.final-cta p {
  max-width: 100%;
  overflow-wrap: break-word;
}

.eyebrow {
  margin: 0 0 12px;
  color: var(--accent);
  text-transform: uppercase;
  letter-spacing: 0.12em;
  font-size: 0.78rem;
  font-weight: 800;
}

h1,
h2,
h3,
p {
  margin-top: 0;
}

h1 {
  max-width: 760px;
  margin-bottom: 22px;
  font-size: clamp(3.4rem, 10vw, 7.2rem);
  line-height: 0.86;
  letter-spacing: -0.075em;
}

h2 {
  margin-bottom: 16px;
  font-size: clamp(2.1rem, 5vw, 4.2rem);
  line-height: 0.95;
  letter-spacing: -0.055em;
}

h3 {
  margin-bottom: 12px;
  font-size: 1.35rem;
  line-height: 1.05;
  letter-spacing: -0.03em;
}

.subheadline {
  max-width: 690px;
  color: var(--text-soft);
  font-size: clamp(1.05rem, 2.3vw, 1.28rem);
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin: 32px 0 14px;
}

.button {
  justify-content: center;
  min-height: 48px;
  padding: 14px 20px;
  border-radius: 999px;
  transition: transform 160ms ease, box-shadow 160ms ease;
}

.button:hover {
  transform: translateY(-1px);
}

.button--primary {
  background: var(--text);
  color: #fff7ed;
  box-shadow: 0 18px 40px rgba(41, 37, 36, 0.22);
}

.button--secondary {
  border: 1px solid var(--border);
  background: var(--surface-strong);
  color: var(--accent-strong);
}

.microcopy,
.benefit-card p,
.workflow span,
.final-cta p {
  color: var(--text-soft);
}

.product-card {
  position: relative;
  display: grid;
  place-items: center;
  min-height: 620px;
}

.product-card::before {
  content: '';
  position: absolute;
  width: 460px;
  height: 460px;
  border-radius: 999px;
  background: radial-gradient(circle, rgba(180, 83, 9, 0.18), transparent 62%);
  filter: blur(10px);
}

.phone-shell {
  position: relative;
  width: min(100%, 340px);
  border: 1px solid rgba(120, 53, 15, 0.18);
  border-radius: 42px;
  padding: 18px;
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.94), rgba(254, 243, 199, 0.78));
  box-shadow: 0 30px 90px rgba(120, 53, 15, 0.24);
}

.phone-topbar {
  display: flex;
  gap: 6px;
  margin-bottom: 18px;
}

.phone-topbar span {
  width: 9px;
  height: 9px;
  border-radius: 999px;
  background: rgba(120, 53, 15, 0.24);
}

.scanner-panel {
  display: grid;
  gap: 14px;
  padding: 18px;
  border-radius: 28px;
  background: #1c1917;
  color: #fff7ed;
}

.scan-frame {
  position: relative;
  height: 210px;
  overflow: hidden;
  border: 1px solid rgba(254, 243, 199, 0.32);
  border-radius: 22px;
  background:
    linear-gradient(90deg, transparent 12%, rgba(255, 247, 237, 0.14) 12% 18%, transparent 18% 27%, rgba(255, 247, 237, 0.18) 27% 32%, transparent 32% 45%, rgba(255, 247, 237, 0.2) 45% 49%, transparent 49% 63%, rgba(255, 247, 237, 0.12) 63% 70%, transparent 70% 100%),
    radial-gradient(circle at center, rgba(180, 83, 9, 0.32), transparent 62%);
}

.scan-line {
  position: absolute;
  left: 18px;
  right: 18px;
  top: 50%;
  height: 2px;
  background: #facc15;
  box-shadow: 0 0 22px rgba(250, 204, 21, 0.9);
}

.scanner-panel p {
  margin: 0;
  color: #fde68a;
  font-weight: 800;
}

.book-preview {
  display: flex;
  gap: 14px;
  align-items: center;
  margin-top: 16px;
  padding: 14px;
  border-radius: 24px;
  background: var(--surface-strong);
  box-shadow: var(--shadow);
}

.cover {
  display: grid;
  width: 64px;
  height: 86px;
  place-items: center;
  border-radius: 12px;
  background: linear-gradient(145deg, #78350f, #f59e0b);
  color: #fff7ed;
  font-weight: 900;
  letter-spacing: 0.18em;
  writing-mode: vertical-rl;
}

.book-preview span {
  display: block;
  margin-top: 4px;
  color: var(--text-soft);
  font-size: 0.9rem;
}

.floating-card {
  position: absolute;
  padding: 12px 16px;
  border: 1px solid var(--border);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: var(--shadow);
  color: var(--accent-strong);
  font-weight: 800;
}

.floating-card--top {
  top: 84px;
  right: 12px;
}

.floating-card--bottom {
  bottom: 80px;
  left: 0;
}

.benefits,
.workflow,
.final-cta {
  max-width: 1180px;
  margin: 0 auto;
  padding: 72px 20px;
}

.section-heading {
  max-width: 760px;
  margin-bottom: 28px;
}

.benefit-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.benefit-card,
.workflow,
.final-cta {
  border: 1px solid var(--border);
  background: var(--surface);
  box-shadow: var(--shadow);
  backdrop-filter: blur(18px);
}

.benefit-card {
  padding: 26px;
  border-radius: 30px;
}

.benefit-icon {
  display: grid;
  width: 44px;
  height: 44px;
  place-items: center;
  margin-bottom: 24px;
  border-radius: 16px;
  background: var(--accent-soft);
  color: var(--accent-strong);
  font-weight: 900;
}

.workflow {
  display: grid;
  grid-template-columns: 0.8fr 1fr;
  gap: 32px;
  border-radius: 36px;
}

.steps {
  display: grid;
  gap: 14px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.steps li {
  display: grid;
  gap: 4px;
  padding: 18px;
  border-radius: 22px;
  background: var(--surface-strong);
}

.final-cta {
  margin-bottom: 60px;
  border-radius: 42px;
  text-align: center;
}

.final-cta p {
  max-width: 620px;
  margin-right: auto;
  margin-left: auto;
}

@media (max-width: 900px) {
  .hero-grid,
  .workflow,
  .benefit-grid {
    grid-template-columns: 1fr;
  }

  .hero-nav {
    margin-bottom: 44px;
  }

  .product-card {
    min-height: 520px;
  }
}

@media (max-width: 560px) {
  .hero,
  .hero-grid,
  .hero-copy,
  .benefits,
  .workflow,
  .final-cta {
    width: 100%;
    max-width: 100%;
    padding-inline: 14px;
  }

  .hero-grid,
  .hero-copy {
    overflow-x: hidden;
  }

  h1 {
    font-size: clamp(2.35rem, 12.5vw, 3rem);
    line-height: 0.96;
    letter-spacing: -0.05em;
  }

  .eyebrow {
    max-width: 100%;
    overflow-wrap: anywhere;
    letter-spacing: 0.08em;
  }

  .hero-actions,
  .brand,
  .nav-cta,
  .button {
    width: 100%;
  }

  .hero-nav {
    align-items: stretch;
    flex-direction: column;
    gap: 12px;
  }

  .phone-shell,
  .book-preview,
  .scanner-panel {
    max-width: 100%;
  }

  .product-card::before {
    width: 300px;
    height: 300px;
  }

  .floating-card {
    position: static;
    max-width: 100%;
    margin-top: 12px;
  }

  .product-card {
    gap: 0;
    max-width: 100%;
    min-height: auto;
  }
}
</style>
