//
//  FlatResponseDataHandler.swift
//  Flat
//
//  Created by xuyunshi on 2021/10/22.
//  Copyright © 2021 agora.io. All rights reserved.
//

import Foundation
import RxRelay
import RxSwift

class FlatResponseHandler: ResponseDataHandler {
    static var jwtExpireSignal = PublishRelay<Void>()
    static var billingServerErrorCodeMap: [Int: FlatApiError] = [
        220000: .ServerFail,
        220001: .ServerFail,
        210000: .ParamsCheckFailed,
        210001: .JWTSignFailed,
        210003: .RoomIsEnded
    ] // Map error to normal flat error, because billing server has some duplicated error.

    func processResponseData<T>(_ data: Data, decoder: JSONDecoder, forResponseType _: T.Type) throws -> T where T: Decodable {
        guard let jsonObj = try JSONSerialization.jsonObject(with: data, options: .allowFragments) as? [String: Any],
              let status = jsonObj["status"] as? Int
        else {
            throw ApiError.serverError(message: "unknown data type")
        }

        if let code = jsonObj["code"] as? Int,
           let error = FlatResponseHandler.billingServerErrorCodeMap[code] ?? FlatApiError(rawValue: code)
        {
            if error == .JWTSignFailed {
                if AuthStore.shared.isLogin { // Will send only once
                    FlatResponseHandler.jwtExpireSignal.accept(())
                }
            }
            throw error
        }
        guard status == 0 else {
            let str = String(data: data, encoding: .utf8)
            throw ApiError.serverError(message: str ?? "")
        }
        decoder.setAnyCodingKey("data")
        return (try decoder.decode(AnyKeyDecodable<T>.self, from: data)).result
    }
}
