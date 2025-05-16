//
//  WKWebViewController.swift
//  Flat
//
//  Created by xuyunshi on 2022/9/20.
//  Copyright © 2022 agora.io. All rights reserved.
//

import UIKit
import WebKit

class WKWebViewController: UIViewController {
    var usingClose = true
    var dismissHandler: (() -> Void)?
    
    init(url: URL, isScrollEnabled: Bool = false) {
        super.init(nibName: nil, bundle: AgoraNativePlugin.pluginBundle)
        webView.load(URLRequest(url: url))
        webView.scrollView.isScrollEnabled = isScrollEnabled
    }

    @available(*, unavailable)
    required init?(coder _: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    @objc func onClickNaviBack() {
        dismissHandler?()
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)

        presentCloseButton?.removeFromSuperview()
        presentTitleLabel?.removeFromSuperview()

        if let _ = navigationController {
            if usingClose {
                navigationItem.leftBarButtonItem = UIBarButtonItem(title: localizeStrings("Close"), style: .plain, target: self, action: #selector(onClickNaviBack))
            }
            webView.snp.remakeConstraints { make in
                make.edges.equalTo(view.safeAreaLayoutGuide)
            }
        } else {
            presentCloseButton = addPresentCloseButton()
            presentTitleLabel = addPresentTitle(title ?? "")
            webView.snp.remakeConstraints { make in
                make.left.right.bottom.equalTo(view.safeAreaLayoutGuide)
                make.top.equalTo(presentCloseButton!.snp.bottom)
            }
        }
    }

    var presentCloseButton: UIButton?
    var presentTitleLabel: UILabel?

    override func viewDidLoad() {
        super.viewDidLoad()
        setupViews()
        webView.navigationDelegate = self
        view.startFlatLoading()
    }

    func setupViews() {
        view.backgroundColor = .color(type: .background)
        webView.backgroundColor = .color(type: .background)
        view.addSubview(webView)
    }

    lazy var webView = WKWebView(frame: .zero)
}

extension WKWebViewController: WKNavigationDelegate {
    func webView(_: WKWebView, didFinish _: WKNavigation!) {
        view.endFlatLoading()
    }
}
