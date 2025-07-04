//
//  PermissionDialogViewController.swift
//  vh_agora_native
//
//  Created by Kien Nguyen on 2/7/25.
//
import UIKit

class PermissionDialogVC: UIViewController {
    init() {
        super.init(nibName: nil, bundle: Bundle.main)
        modalPresentationStyle = .overCurrentContext
        modalTransitionStyle = .crossDissolve
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupView()
        // Do any additional setup after loading the view.
    }
    
    func setupView() {
        let emptyBtn = UIButton()
        emptyBtn.addTarget(self, action: #selector(onClickClose), for: .touchUpInside)
        emptyBtn.backgroundColor = .black.withAlphaComponent(0.6)
        emptyBtn.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(emptyBtn)
        NSLayoutConstraint.activate([
            emptyBtn.topAnchor.constraint(equalTo: view.topAnchor),
            emptyBtn.bottomAnchor
                .constraint(equalTo: view.bottomAnchor),
            emptyBtn.leftAnchor.constraint(equalTo: view.leftAnchor),
            emptyBtn.rightAnchor.constraint(equalTo: view.rightAnchor)
        ])
        
        view.addSubview(contentView)
        NSLayoutConstraint.activate([
            contentView.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            contentView.centerYAnchor.constraint(equalTo: view.centerYAnchor),
            contentView.heightAnchor.constraint(lessThanOrEqualTo: view.heightAnchor, constant: -32),
            contentView.widthAnchor.constraint(equalToConstant: 340),
            {
                let preferredHeight = contentView.heightAnchor.constraint(equalToConstant: 100)
                preferredHeight.priority = .defaultLow
                return preferredHeight
            }()
        ])
        
        let stackView = UIStackView()
        stackView.translatesAutoresizingMaskIntoConstraints = false
        stackView.axis = .vertical
        stackView.alignment = .center
        contentView.addSubview(stackView)
        NSLayoutConstraint.activate([
            stackView.topAnchor
                .constraint(equalTo: contentView.topAnchor, constant: 8),
            stackView.bottomAnchor
                .constraint(equalTo: contentView.bottomAnchor, constant: -8),
            stackView.leadingAnchor.constraint(equalTo: contentView.leadingAnchor, constant: 8),
            stackView.trailingAnchor.constraint(equalTo: contentView.trailingAnchor, constant: -8),
        ])
        
        stackView.addArrangedSubview(closeButton)
        closeButton.widthAnchor
            .constraint(equalTo: stackView.widthAnchor, multiplier: 1).isActive = true
        stackView.addArrangedSubview(imageView)
        stackView.addArrangedSubview(titleLbl)
        stackView.setCustomSpacing(8, after: titleLbl)
        stackView.addArrangedSubview(firstStepLbl)
        stackView.addArrangedSubview(secondStepLbl)
        stackView.setCustomSpacing(24, after: secondStepLbl)
        stackView.addArrangedSubview(goToSettingBtn)
        goToSettingBtn.widthAnchor
            .constraint(equalTo: stackView.widthAnchor, multiplier: 1).isActive = true
    }
    
    @objc func onClickClose() {
        dismiss(animated: true)
    }
    
    @objc func onClickGoToSetting() {
        if let url = URL(string: UIApplication.openSettingsURLString) {
            UIApplication.shared.open(url)
        }
        dismiss(animated: true)
    }
    
    lazy var contentView: UIView = {
        let view = UIView()
        view.backgroundColor = .white
        view.clipsToBounds = true
        view.layer.cornerRadius = 6
        view.translatesAutoresizingMaskIntoConstraints = false
        return view
    }()
    
    lazy var closeButton: UIView = {
        let closeButton = UIButton(type: .custom)
        closeButton.setImage(UIImage.fromPlugin(named: "close-bold"), for: .normal)
        closeButton.tintColor = .gray
        closeButton.addTarget(self, action: #selector(onClickClose), for: .touchUpInside)
        closeButton.translatesAutoresizingMaskIntoConstraints = false
        let closeContainer = UIView()
        closeContainer.addSubview(closeButton)
        closeContainer.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            closeContainer.heightAnchor.constraint(equalToConstant: 24),
            closeButton.rightAnchor.constraint(equalTo: closeContainer.rightAnchor),
            closeButton.topAnchor.constraint(equalTo: closeContainer.topAnchor),
            closeButton.bottomAnchor
                .constraint(equalTo: closeContainer.bottomAnchor),
        ])
        return closeContainer
    }()
    
    lazy var titleLbl: UILabel = {
        let label = UILabel()
        label.textColor = .black
        label.text = "Hướng dẫn cấp quyền\n Camera và Micro"
        label.numberOfLines = 2
        label.font = .boldSystemFont(ofSize: 18)
        label.translatesAutoresizingMaskIntoConstraints = false
        label.textAlignment = .center
        return label
    }()
    
    lazy var firstStepLbl: UILabel = {
        let text = "1. Vào cài đặt ứng dụng RINO EDU"
        let attributedString = NSMutableAttributedString(string: text)
        let boldFont = UIFont.boldSystemFont(ofSize: 14)
        let regularFont = UIFont.systemFont(ofSize: 14)
        attributedString.addAttribute(.font, value: regularFont, range: NSRange(location: 0, length: text.count))
        if let rinoRange = text.range(of: "RINO EDU") {
            let nsRange = NSRange(rinoRange, in: text)
                    attributedString.addAttribute(.font, value: boldFont, range: nsRange)
                }
        let label = UILabel()
        label.textColor = .black
        label.attributedText = attributedString
        label.translatesAutoresizingMaskIntoConstraints = false
        label.textAlignment = .center
        return label
    }()
    
    lazy var secondStepLbl: UILabel = {
        let text = "2. Bật nút Camera và Micro"
        let attributedString = NSMutableAttributedString(string: text)
        let boldFont = UIFont.boldSystemFont(ofSize: 14)
        let regularFont = UIFont.systemFont(ofSize: 14)
        attributedString.addAttribute(.font, value: regularFont, range: NSRange(location: 0, length: text.count))
        if let rinoRange = text.range(of: "Camera") {
            let nsRange = NSRange(rinoRange, in: text)
                    attributedString.addAttribute(.font, value: boldFont, range: nsRange)
                }
        if let rinoRange = text.range(of: "Micro") {
            let nsRange = NSRange(rinoRange, in: text)
                    attributedString.addAttribute(.font, value: boldFont, range: nsRange)
                }
        let label = UILabel()
        label.textColor = .black
        label.attributedText = attributedString
        label.translatesAutoresizingMaskIntoConstraints = false
        label.textAlignment = .center
        return label
    }()
    
    lazy var goToSettingBtn: UIButton = {
        let button = UIButton(type: .system)
        button.setTitle("Vào cài đặt", for: .normal)
        button.addTarget(self, action: #selector(onClickGoToSetting), for: .touchUpInside)
        button.translatesAutoresizingMaskIntoConstraints = false
        button.layer.cornerRadius = 5
        button.layer.masksToBounds = true
        button.backgroundColor = UIColor(red: 255/255.0, green: 102/255.0, blue: 9/255.0, alpha: 1.0)
        button.translatesAutoresizingMaskIntoConstraints = false
        button.heightAnchor.constraint(equalToConstant: 40).isActive = true
        button.setTitleColor(.white, for: .normal)
        
        return button
    }()
    
    lazy var imageView: UIImageView = {
        let imageView = UIImageView()
        imageView.image = UIImage.fromPlugin(named: "mic_phone_permission")
        imageView.contentMode = .scaleAspectFit
        imageView.translatesAutoresizingMaskIntoConstraints = false
        return imageView
    }()
    /*
     // MARK: - Navigation
     
     // In a storyboard-based application, you will often want to do a little preparation before navigation
     override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
     // Get the new view controller using segue.destination.
     // Pass the selected object to the new view controller.
     }
     */
    
}
