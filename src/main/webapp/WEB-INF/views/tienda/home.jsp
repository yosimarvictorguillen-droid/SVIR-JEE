<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.svir.jee.model.Producto" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("tituloPagina", "Inicio");
    List<Producto> productosDestacados = (List<Producto>) request.getAttribute("productosDestacados");
    if (productosDestacados == null) productosDestacados = new java.util.ArrayList<>();
%>
<%@ include file="/WEB-INF/views/tienda/common/tienda-header.jspf" %>

<!-- ================= HERO ================= -->
<section class="hero-v2">
    <div class="row align-items-center g-4">
        <div class="col-lg-6">
            <span class="pill-badge"><i class="bi bi-star-fill"></i> Reposteria artesanal &middot; Lima</span>
            <h1>Postres hechos con amor,<br>listos para ti</h1>
            <p class="text-muted fs-5 mt-3" style="max-width:480px;">
                Tortas, cupcakes, brownies y mas &mdash; preparados con ingredientes de calidad
                y una presentacion que enamora. Pide en linea facil y rapido.
            </p>
            <div class="d-flex gap-2 flex-wrap mt-4 mb-4">
                <a href="<%= ctx %>/catalogo" class="btn btn-amber btn-lg">Ver catalogo</a>
                <button type="button" class="btn btn-outline-amber btn-lg" data-bs-toggle="modal"
                        data-bs-target="#authModal" onclick="mostrarTabAuth('tabRegistro')">
                    Registrarme gratis
                </button>
            </div>
            <div class="d-flex gap-3 flex-wrap">
                <div class="mini-stat"><strong>+200</strong><span>Pedidos felices</span></div>
                <div class="mini-stat"><strong>100%</strong><span>Artesanal</span></div>
                <div class="mini-stat"><strong>Fresh</strong><span>Del dia</span></div>
            </div>
        </div>
        <div class="col-lg-6">
            <div class="hero-collage">
                <%
                    String img1 = productosDestacados.size() > 0 ? productosDestacados.get(0).getImagenUrl() : null;
                    String img2 = productosDestacados.size() > 1 ? productosDestacados.get(1).getImagenUrl() : null;
                    String img3 = productosDestacados.size() > 2 ? productosDestacados.get(2).getImagenUrl() : null;
                %>
                <% if (img1 != null) { %><img class="collage-main" src="<%= ctx %><%= img1 %>" alt="Postre"><% } %>
                <% if (img2 != null) { %><img class="collage-top" src="<%= ctx %><%= img2 %>" alt="Postre"><% } %>
                <% if (img3 != null) { %><img class="collage-bottom" src="<%= ctx %><%= img3 %>" alt="Postre"><% } %>
                <span class="collage-tag"><i class="bi bi-circle-fill" style="color:#eab308;font-size:.5rem;"></i> Hecho a mano</span>
            </div>
        </div>
    </div>
</section>

<!-- ================= COMO FUNCIONA ================= -->
<section class="text-center py-5">
    <span class="pill-badge-light">Simple y rapido</span>
    <h2 class="fw-bold">¿Como hago mi pedido?</h2>
    <p class="text-muted">Comprar en Dulce Momento es muy facil.</p>

    <div class="row g-4 mt-3">
        <div class="col-6 col-md-3">
            <div class="step-card">
                <span class="step-number">1</span>
                <div class="step-icon"><i class="bi bi-person-fill"></i></div>
                <h6 class="fw-bold">Registrate</h6>
                <p class="text-muted small mb-0">Crea tu cuenta con nombre y DNI en segundos.</p>
            </div>
        </div>
        <div class="col-6 col-md-3">
            <div class="step-card">
                <span class="step-number">2</span>
                <div class="step-icon"><i class="bi bi-search"></i></div>
                <h6 class="fw-bold">Explora</h6>
                <p class="text-muted small mb-0">Navega el catalogo y elige tus postres favoritos.</p>
            </div>
        </div>
        <div class="col-6 col-md-3">
            <div class="step-card">
                <span class="step-number">3</span>
                <div class="step-icon"><i class="bi bi-cart-check-fill"></i></div>
                <h6 class="fw-bold">Confirma</h6>
                <p class="text-muted small mb-0">Revisa tu carrito y confirma tu pedido.</p>
            </div>
        </div>
        <div class="col-6 col-md-3">
            <div class="step-card">
                <span class="step-number">4</span>
                <div class="step-icon"><i class="bi bi-emoji-smile-fill"></i></div>
                <h6 class="fw-bold">¡Disfruta!</h6>
                <p class="text-muted small mb-0">Recibe tu pedido recien preparado con amor.</p>
            </div>
        </div>
    </div>
</section>

<!-- ================= PRODUCTOS DESTACADOS ================= -->
<section class="py-5" style="background:#fef6ec;border-radius:1.5rem;">
    <div class="text-center mb-4">
        <span class="pill-badge-light">Del horno para ti</span>
        <h2 class="fw-bold">Nuestros productos</h2>
        <p class="text-muted">Seleccion artesanal preparada con ingredientes de primera calidad.</p>
    </div>

    <div class="row g-4">
        <%
            for (Producto p : productosDestacados) {
        %>
        <div class="col-6 col-md-3">
            <div class="producto-card">
                <% if (p.getImagenUrl() != null) { %>
                    <div class="img-wrap" style="background:none;">
                        <img src="<%= ctx %><%= p.getImagenUrl() %>" alt="<%= p.getNombre() %>"
                             style="width:100%;height:100%;object-fit:cover;">
                    </div>
                <% } else { %>
                    <div class="img-wrap"><%= p.getNombre().substring(0, 1) %></div>
                <% } %>
                <div class="body">
                    <h6 class="mb-1"><%= p.getNombre() %></h6>
                    <p class="fw-bold mb-0">S/ <%= p.getPrecio() %></p>
                </div>
            </div>
        </div>
        <% } %>
        <% if (productosDestacados.isEmpty()) { %>
        <div class="col-12 text-center text-muted">No se pudieron cargar los productos.</div>
        <% } %>
    </div>

    <div class="text-center mt-4">
        <a href="<%= ctx %>/catalogo" class="btn btn-amber">Ver catalogo completo <i class="bi bi-arrow-right"></i></a>
    </div>
</section>

<!-- ================= TESTIMONIOS ================= -->
<section class="text-center py-5">
    <span class="pill-badge-light">Opiniones</span>
    <h2 class="fw-bold">Lo que dicen nuestros clientes</h2>

    <div class="row g-4 mt-3">
        <div class="col-md-4">
            <div class="testimonial-card">
                <i class="bi bi-quote quote-icon"></i>
                <p class="mt-2">La presentacion fue hermosa y el sabor increible. Volveria a pedir sin dudar.</p>
                <div class="d-flex align-items-center gap-2 mt-3">
                    <span class="avatar-circle" style="background:#d97706;">M</span>
                    <div>
                        <div class="fw-semibold small">Maria R.</div>
                        <div class="text-warning small">★★★★★</div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="testimonial-card">
                <i class="bi bi-quote quote-icon"></i>
                <p class="mt-2">Muy facil hacer el pedido y los postres llegaron en excelente estado.</p>
                <div class="d-flex align-items-center gap-2 mt-3">
                    <span class="avatar-circle" style="background:#0891b2;">C</span>
                    <div>
                        <div class="fw-semibold small">Carlos T.</div>
                        <div class="text-warning small">★★★★★</div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <div class="testimonial-card">
                <i class="bi bi-quote quote-icon"></i>
                <p class="mt-2">El diseno se ve bonito y comprar es super rapido. Me encanto la experiencia.</p>
                <div class="d-flex align-items-center gap-2 mt-3">
                    <span class="avatar-circle" style="background:#a21caf;">A</span>
                    <div>
                        <div class="fw-semibold small">Andrea P.</div>
                        <div class="text-warning small">★★★★★</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- ================= CTA FINAL ================= -->
<section class="cta-banda">
    <div class="row align-items-center g-3">
        <div class="col-md-8">
            <span class="pill-badge-dark">Haz tu pedido hoy</span>
            <h3>Sorprende con un postre hecho especialmente para ese momento</h3>
            <p class="mb-0 opacity-75">Registrate, explora el catalogo y confirma tu pedido en pocos pasos.</p>
        </div>
        <div class="col-md-4 text-md-end">
            <a href="<%= ctx %>/catalogo" class="btn btn-light">Ver catalogo</a>
            <button type="button" class="btn btn-outline-light" data-bs-toggle="modal"
                    data-bs-target="#authModal" onclick="mostrarTabAuth('tabRegistro')">
                Registrarme gratis
            </button>
        </div>
    </div>
</section>

<%@ include file="/WEB-INF/views/tienda/common/tienda-footer.jspf" %>
