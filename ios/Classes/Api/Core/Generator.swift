//
//  Generator.swift
//  Flat
//
//  Created by xuyunshi on 2021/10/22.
//  Copyright © 2021 agora.io. All rights reserved.
//

import Foundation
import RxSwift

protocol Generator {
    func generateRequest<T: Request>(fromApi api: T) throws -> URLRequest
}

extension Generator {
    func generateObservableRequest(fromApi api: some Request) -> Observable<URLRequest> {
        .create { observer in
            do {
                let request: URLRequest = try self.generateRequest(fromApi: api)
                observer.onNext(request)
                observer.onCompleted()
            } catch {
                observer.onError(error)
            }
            return Disposables.create()
        }
    }
}
