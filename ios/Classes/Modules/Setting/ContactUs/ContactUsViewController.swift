//
//  ContactUsViewController.swift
//  flat
//
//  Created by xuyunshi on 2021/10/15.
//  Copyright © 2021 agora.io. All rights reserved.
//

import UIKit

class ContactUsViewController: UIViewController {
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        textView.becomeFirstResponder()
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        setupViews()
    }

    @IBAction func onClickContactUs(_: Any) {
        guard let text = textView.text, !text.isEmpty else { return }
        // TODO: api
    }

    func setupViews() {
        title = localizeStrings("Contact Us")
    }

    @IBOutlet var textView: UITextView!
}
